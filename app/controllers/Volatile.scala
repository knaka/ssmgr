package controllers

import javax.inject._
// import play.api._
import play.api.mvc._

@Singleton
class Volatile @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.volatile("Volatile page."))
  }
  def post = Action(parse.multipartFormData) { request =>
    request.body.file("the_file").map { bin =>
      import java.io.File
      val filename = bin.filename
      val contentType = bin.contentType
      bin.ref.moveTo(new File(s"/tmp/$filename"))
      Ok("File uploaded")
    }.getOrElse {
      BadRequest("NG")
    }
  }
}
