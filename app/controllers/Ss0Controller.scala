package controllers

import javax.inject._
// import play.api._
import play.api.mvc._

import java.sql.{DriverManager, Connection, Statement, ResultSet, SQLException}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class Ss0Controller @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    var rows: List[List[String]] = List()
    try {
      Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
      var con = DriverManager.getConnection(
        "jdbc:mysql://localhost/ssmgr?user=ssmgr&password=hogehoge" );
      try {
        var stmt = con.createStatement()
        var rs: ResultSet = stmt.executeQuery("SELECT * FROM ss0")
        while (rs.next()) {
          val row = List(
            rs.getString(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4),
            rs.getString(5),
            rs.getString(6),
            rs.getString(7),
            rs.getString(8),
            rs.getString(9),
            rs.getString(10),
            rs.getString(11),
            rs.getString(12),
            rs.getString(13),
            rs.getString(14),
            rs.getString(15),
            rs.getString(16),
            rs.getString(17),
            rs.getString(18),
            rs.getString(19),
            rs.getString(20),
            rs.getString(21),
            rs.getString(22),
            rs.getString(23),
            rs.getString(24),
            rs.getString(25),
            rs.getString(26) )
          rows = rows :+ row
        }
        stmt.close()
      } catch {
        case e: SQLException => println("Database error 0: " + e)
        case e: Throwable => {
          println("Some other exception type:")
          e.printStackTrace()
        }
      } finally {
        // con.close()
      }
    } catch {
      case e: SQLException => println("Database error 1: " + e)
      case e: Throwable => {
        println("Some other exception type:")
        e.printStackTrace()
      }
    }
    Ok(views.html.table("Spreadsheet data in table “ss0”. Baz", rows))
  }
}
