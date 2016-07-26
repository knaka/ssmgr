package controllers

import javax.inject._
// import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._

import scala.concurrent._
import ExecutionContext.Implicits.global

import anorm._
// import anorm.SqlParser._
// import anorm.{SQL, SqlParser}
import play.api.db.DB
import java.util.UUID
import java.util.UUID._

case class SheetData(
  name: String,
  readable_name: String
)

object MyUtil {
  def getId(name: String) = {
    DB.withConnection { implicit connection =>
      val id: Int = SQL("""
        SELECT id
        FROM sheet
        WHERE name = {name}
      """).on('name -> name).as(SqlParser.int("id").single)
      id
    }
  }
  def getIdName(name: String): (Int, String) = {
    val id = DB.withConnection { implicit connection =>
      SQL("""
        SELECT id
        FROM sheet
        WHERE name = {name}
      """).on('name -> name).as(SqlParser.int("id").single)
    }
    val readable_name = DB.withConnection { implicit connection =>
      SQL("""
        SELECT readable_name
        FROM sheet
        WHERE name = {name}
      """).on('name -> name).as(SqlParser.str("readable_name").single)
    }
    (id, readable_name)
  }
}

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
    }.map { result =>
      result match {
        case Right(l) => Ok(views.html.sheets(l))
        case Left(_) => InternalServerError("Error")
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
        val name: Option[Long] = SQL("""
          INSERT INTO sheet (name, readable_name) VALUES ({name}, {readable_name})
        """).on(
          'name -> sheetData.name,
          'readable_name -> sheetData.readable_name
        ).executeInsert()
      }
      Redirect(controllers.routes.Sheet.list)
    }
  }
  def read(name: String) = Action {
    // val id = MyUtil.getId(name)
    val (sheet_id, readable_name) = MyUtil.getIdName(name)
    DB.withConnection { implicit c =>
      val header: List[String] = SQL("""
        SELECT readable_name
        FROM header
        WHERE sheet_id = {sheet_id}
        ORDER BY col_idx
      """).on(
        'sheet_id -> sheet_id
      ).fold(List[String]()) { (list, row) =>
        list :+ row[String]("readable_name")
      } match {
        case Right(l) => l
        case Left(_) => List[String]()
      }
      val range: List[Int] = SQL("""
        SELECT DISTINCT row_idx FROM body ORDER BY row_idx;
      """).fold(List[Int]()) {(l, row) =>
        l :+ row[Int]("row_idx")
      } match {
        case Right(l) => l
        case Left(_) => List[Int]()
      }
      val body: List[List[(String, String)]] = range.map { row_idx =>
        SQL("""
          SELECT value
          FROM body
          WHERE
            sheet_id = {sheet_id} AND
            row_idx = {row_idx}
          ORDER BY col_idx
        """).on(
          'sheet_id -> sheet_id,
          'row_idx -> row_idx
        ).fold(List[(String, String)]()) { (list, row) =>
          val value = row[String]("value")
          // val scode = """^ *([0-9][0-9][0-9][0-9]) *$""".r
          val scode = """^ *([0-9][0-9][0-9][0-9])\.[TQ]\b *$""".r
          list :+ (value -> (
            value match {
              case scode(code) => "http://stocks.finance.yahoo.co.jp/stocks/detail/?code=" + code
              case _ => ""
            }
          ))
        } match {
          case Right(l) => l
          case Left(_) => List[(String, String)]()
        }
      }
      Ok(views.html.sheet(sheet_id, name, readable_name, header, body))
    }
  }
  def update(name: String) = Action(parse.multipartFormData) { request =>
    val (sheet_id, sheet_readable_name) = MyUtil.getIdName(name)
    request.body.file("the_file").map { bin =>
      import java.io.File
      val filename = bin.filename
      val contentType = bin.contentType
      // val filenameNew = s"/tmp/$filename"
      // bin.ref.moveTo(new File(filenameNew))
      import controllers.excel._
      val xlsx = XlsxFile(bin.ref.file)
      DB.withConnection { implicit connection =>
        val result: Int = SQL("""
          UPDATE sheet SET col_max = {colmax} WHERE name = {name}
        """).on(
          'colmax -> xlsx.colmax,
          'name -> name
        ).executeUpdate()
        var col_idx = 0
        SQL("""
          DELETE FROM header WHERE sheet_id = {sheet_id};
        """).on('sheet_id -> sheet_id).execute()
        SQL("""
          DELETE FROM body WHERE sheet_id = {sheet_id};
        """).on('sheet_id -> sheet_id).execute()
        xlsx.header.map {readable_name =>
          val opt = SQL("""
            INSERT INTO header (sheet_id, col_idx, readable_name)
            VALUES ({sheet_id}, {col_idx}, {readable_name})
          """).on(
            'sheet_id -> sheet_id,
            'col_idx -> col_idx,
            'readable_name -> readable_name
          ).executeInsert()
          col_idx = col_idx + 1
        }
        var row_idx = 0
        xlsx.body.map {row =>
          var col_idx = 0
          row.map {value =>
            val opt = SQL("""
              INSERT INTO body (sheet_id, row_idx, col_idx, value)
              VALUES ({sheet_id}, {row_idx}, {col_idx}, {value})
            """).on(
              'sheet_id -> sheet_id,
              'row_idx -> row_idx,
              'col_idx -> col_idx,
              'value -> value
            ).executeInsert()
            col_idx = col_idx + 1
          }
          row_idx = row_idx + 1
        }
      }
      Redirect(controllers.routes.Sheet.read(name))
    }.getOrElse {
      BadRequest("NG")
    }
  }
}
