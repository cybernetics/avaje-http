package io.avaje.http.generator.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PathSegments {

  static final PathSegments EMPTY = new PathSegments(new Chunks(), Collections.emptySet());

  static PathSegments parse(String fullPath) {

    Set<Segment> segments = new LinkedHashSet<>();

    Chunks chunks = new Chunks();
    if ("/".equals(fullPath)) {
      chunks.literal("/");

    } else {
      for (String section : fullPath.split("/")) {
        if (!section.isEmpty()) {
          chunks.literal("/");
          if (section.startsWith(":")) {
            final String name = section.substring(1);
            Segment segment = createSegment(name);
            segments.add(segment);
            chunks.named(segment.path(name));

          } else if ((section.startsWith("{") && (section.endsWith("}")))) {
            String name = section.substring(1, section.length() - 1);
            Segment segment = createSegment(name);
            segments.add(segment);
            chunks.named(segment.path(name));

          } else {
            chunks.literal(section);
          }
        }
      }
    }

    return new PathSegments(chunks, segments);
  }

  private static Segment createSegment(String val) {
    String[] matrixSplit = val.split(";");
    if (matrixSplit.length == 1) {
      return new Segment(matrixSplit[0]);
    }
    Set<String> matrixKeys = new HashSet<>(Arrays.asList(matrixSplit).subList(1, matrixSplit.length));
    return new Segment(matrixSplit[0], matrixKeys);
  }

  private final Chunks chunks;

  private final Set<Segment> segments;

  private final List<Segment> withMatrixs = new ArrayList<>();

  private final Set<String> allNames = new HashSet<>();

  private PathSegments(Chunks chunks, Set<Segment> segments) {
    this.chunks = chunks;
    this.segments = segments;
    for (Segment segment : segments) {
      segment.addNames(allNames);
      if (segment.hasMatrixParams()) {
        withMatrixs.add(segment);
      }
    }
  }


  public boolean contains(String varName) {
    return allNames.contains(varName);
  }

  public List<Segment> matrixSegments() {
    return withMatrixs;
  }

  public Segment segment(String varName) {

    for (Segment segment : segments) {
      if (segment.isPathParameter(varName)) {
        return segment;
      }
    }
    return null;
  }

  /**
   * Return full path with <code>{}</code for named path params.
   */
  public String fullPath() {
    return fullPath("{", "}");
  }

  /**
   * Return full path with colon for named path params (Javalin).
   */
  public String fullPathColon() {
    return fullPath(":", "");
  }

  private String fullPath(String prefix, String suffix) {
    return chunks.fullPath(prefix, suffix);
  }

  public static class Segment {

    private final String name;

    /**
     * Matrix keys.
     */
    private final Set<String> matrixKeys;

    /**
     * Variable names the matrix map to (Java method param names).
     */
    private final Set<String> matrixVarNames;

    Segment(String name) {
      this.name = name;
      this.matrixKeys = null;
      this.matrixVarNames = null;
    }

    Segment(String name, Set<String> matrixKeys) {
      this.name = name;
      this.matrixKeys = matrixKeys;
      this.matrixVarNames = new HashSet<>();
      for (String key : matrixKeys) {
        matrixVarNames.add(combine(name, key));
      }
    }

    void addNames(Set<String> allNames) {
      allNames.add(name);
    }

    boolean hasMatrixParams() {
      return matrixKeys != null && !matrixKeys.isEmpty();
    }

    private String combine(String name, String key) {
      return name + Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }

    Set<String> matrixKeys() {
      return matrixKeys;
    }

    String name() {
      return name;
    }

    boolean isPathParameter(String varName) {
      return name.equals(varName) || (matrixKeys != null && (matrixVarNames.contains(varName) || matrixKeys.contains(varName)));
    }

    /**
     * Reading the value from a segment (rather than directly from pathParam).
     */
    void writeGetVal(Append writer, String varName, PlatformAdapter platform) {
      if (!hasMatrixParams()) {
        platform.writeReadParameter(writer, ParamType.PATHPARAM, name);
      } else {
        writer.append("%s_segment.", name);
        if (name.equals(varName)) {
          writer.append("val()");
        } else {
          writer.append("matrix(\"%s\")", matrixKey(varName));
        }
      }
    }

    private String matrixKey(String varName) {
      if (!varName.startsWith(name)) {
        return varName;
      }
      String key = varName.substring(name.length());
      return Character.toLowerCase(key.charAt(0)) + key.substring(1);
    }

    public void writeCreateSegment(Append writer, PlatformAdapter platform) {
      writer.append(platform.indent());
      writer.append("  PathSegment %s_segment = PathSegment.of(", name);
      platform.writeReadParameter(writer, ParamType.PATHPARAM, name + "_segment");
      writer.append(");").eol();
    }

    boolean isRequired(String varName) {
      return name.equals(varName);
    }

    String path(String section) {
      if (!hasMatrixParams()) {
        return section;
      }
      return name + "_segment";
    }
  }

  private static class Chunks {
    private final List<Chunk> chunks = new ArrayList<>();

    void named(String name) {
      chunks.add(new Chunk(name));
    }

    void literal(String val) {
      chunks.add(new LiteralChunk(val));
    }

    String fullPath(String prefix, String suffix) {
      StringBuilder sb = new StringBuilder();
      for (Chunk chunk : chunks) {
        chunk.append(sb, prefix, suffix);
      }
      return sb.toString();
    }
  }

  private static class LiteralChunk extends Chunk {
    private LiteralChunk(String value) {
      super(value);
    }

    @Override
    void append(StringBuilder fullPath, String prefix, String suffix) {
      fullPath.append(value);
    }
  }

  private static class Chunk {
    final String value;
    private Chunk(String value) {
      this.value = value;
    }

    void append(StringBuilder fullPath, String prefix, String suffix) {
      fullPath.append(prefix).append(value).append(suffix);
    }
  }

}
