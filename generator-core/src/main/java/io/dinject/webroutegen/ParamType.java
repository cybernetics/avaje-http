package io.dinject.webroutegen;

public enum ParamType {

  BODY("body", "body"),
  PATHPARAM("pathParam", "path"),
  FORM("form", "form"),
  BEANPARAM("beanParam", "bean"),
  QUERYPARAM("queryParam", "query"),
  FORMPARAM("formParam", "form"),
  COOKIE("cookie", "cookie"),
  HEADER("header", "header");

  private final String code;
  private final String type;

  ParamType(String code, String type) {
    this.code = code;
    this.type = type;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return code;
  }
}