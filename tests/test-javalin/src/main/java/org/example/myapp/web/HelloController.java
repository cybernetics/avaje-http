package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Default;
import io.avaje.http.api.Delete;
import io.avaje.http.api.Form;
import io.avaje.http.api.Get;
import io.avaje.http.api.MediaType;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.http.api.QueryParam;
import io.javalin.http.Context;
import io.swagger.v3.oas.annotations.Hidden;
import org.example.myapp.service.MyService;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Hello resource manager.
 * <p>
 * Simple API for Hello resources.
 */
//@Hidden
@Valid
@Controller
@Path("/hello")
class HelloController {

  private final MyService myService;

  @Inject
  HelloController(MyService myService) {
    this.myService = myService;
  }

  @Produces(MediaType.TEXT_PLAIN)
  @Get("message")
  String getPlainMessage() {
    return "hello world";
  }

  /**
   * Return the Hello DTO.
   *
   * @param id         The hello Id.
   * @param date       The name of the hello
   * @param otherParam Optional other parameter
   * @return The Hello DTO given the id and name.
   * @deprecated Please migrate away
   */
  @Get("/:id/:date")
  HelloDto hello(int id, LocalDate date, String otherParam) {
    return new HelloDto(id, date.toString(), otherParam);
  }

  /**
   * Find Hellos by name.
   *
   * @param name       The name to search for
   * @param myParam My option parameter
   * @return The Hellos that we found.
   */
  @Get("/findbyname/{name}")
  List<HelloDto> findByName(String name, @QueryParam("my-param") @Default("one") String myParam) {
    return new ArrayList<>();
  }

  /**
   * Simple example post with JSON body response.
   */
  @Post
  HelloDto post(HelloDto dto) {
    dto.name = "posted";
    return dto;
  }

  /**
   * Save the hello using json body.
   *
   * @param foo The hello doo id
   * @param dto The hello body as json
   */
//  @Roles({ADMIN})
  @Post("/savebean/:foo")
  void saveBean(String foo, HelloDto dto, Context context) {
    // save hello data ...
    System.out.println("save " + foo + " dto:" + dto);
    requireNonNull(foo);
    requireNonNull(dto);
    requireNonNull(context);
  }

  /**
   * Create the new Hello using a form.
   */
  @Post("saveform")
  @Form
  void saveForm(HelloForm helloForm) {
    System.out.println("saving " + helloForm);
  }

  @Form @Post("mySave")
  void saveForm324(@Default("junk") String name, String email, String url) {
    System.out.println("name " + name + " email:" + email + " url:" + url);
  }


  @Post("saveform2")
  @Form
  void saveForm2(String name, String email, String url) {
    System.out.println("name " + name + " email:" + email + " url:" + url);
  }

  @Post("saveform3")
  @Form
  HelloDto saveForm3(HelloForm helloForm) {
    return new HelloDto(52, helloForm.name, helloForm.email);
  }

  @Hidden
  @Get
  List<HelloDto> getAll() {
    return myService.findAll();
  }

  //  @Hidden
  @Delete(":id")
  void deleteById(int id) {
    System.out.println("deleting " + id);
  }

  @Produces("text/plain")
  @Get("/withMatrix/:year;author;country/:other")
  String getWithMatrixParam(int year, String author, String country, String other, String extra) {
    return "yr:" + year + " au:" + author + " co:" + country + " other:" + other + " extra:" + extra;
  }
}
