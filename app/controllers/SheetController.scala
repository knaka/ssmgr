package controllers

import javax.inject._
// import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._

import scala.concurrent._
import ExecutionContext.Implicits.global

import anorm.{SQL, SqlParser}
import play.api.db.DB
import java.util.UUID
import java.util.UUID._

case class SheetData(
  name: String,
  readable_name: String
)

@Singleton
class Sheet @Inject() extends Controller {
  def list = Action.async { request =>
    var l: List[(String, String)] = List()
    Future {
      DB.withConnection { implicit c =>
        SQL("SELECT name, readable_name FROM sheet").fold(List[SheetData]()) { (list, row) =>
          list :+ SheetData(row[String]("name"), row[String]("readable_name"))
        }
      }
    }.map { r =>
      r match {
        case Left(_) => InternalServerError("Error")
        case Right(l) => Ok(views.html.sheets(l))
      }
    }
  }
  def sheetForm(uuid: UUID) = {
    lazy val uuid_str =
      "%x".format(uuid.getMostSignificantBits) +
      "%x".format(uuid.getLeastSignificantBits)
    Form(
      mapping(
        "name" -> default(text, uuid_str),
        "readable_name" -> default(text, uuid_str)
      )(SheetData.apply)(SheetData.unapply)
    )
  }
  def create = Action.async { implicit request =>
    lazy val uuid = randomUUID
    val sheetData = sheetForm(uuid).bindFromRequest.get
    Future {
      DB.withConnection { implicit connection =>
        val id: Option[Long] = SQL("""
          INSERT INTO sheet (name, readable_name) VALUES ({name}, {readable_name})
        """).on(
          'name -> sheetData.name,
          'readable_name -> sheetData.readable_name
        ).executeInsert()
      }
      Redirect(controllers.routes.Sheet.list)
    }
  }
  def read(id: String) = Action {
    Ok(views.html.sheet(id))
  }
  def update(id: String) = Action {
    Ok("HOGE")
  }
}
