package controllers.excel

import java.io.File
import org.apache.poi.ss.usermodel._
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.ss.usermodel.IndexedColors
import collection.JavaConversions._

/*
:reset
:paste -raw app/controllers/excel/Util.scala
import controllers.excel.XlsxFile
val xlsx = controllers.excel.XlsxFile.apply()

*/

object XlsxFile {
  class XlsxFile(
    file: File
  ) {
    // val filename: String = "/Users/knaka/doc/2016/sakuragumi/dummy_data_2-mod.xlsx"; val file = new File(filename)
    // val filename: String = "/Users/knaka/doc/2016/sakuragumi/simple.xlsx"; val file = new File(filename)
    lazy val formatter = new DataFormatter()
    lazy val workbook = WorkbookFactory.create(file)
    lazy val sheet = workbook.getSheetAt(0)
    lazy val headerPoi::bodyPoi = sheet.iterator.toList
    lazy val colmax = { headerPoi.iterator.toList.length }
    // val header = headerPoi.iterator.toList.map {cell =>
    //   formatter.formatCellValue(cell)
    // }
    val header = headerPoi.iterator.toList /* .filter { cell =>
      // headerPoi.toList(0).getCellStyle.getFillForegroundColorColor.asInstanceOf[XSSFColor]
      // headerPoi.toList(1).getCellStyle.getFillForegroundColorColor.asInstanceOf[XSSFColor]
      // headerPoi.toList(0).getCellStyle.getFillForegroundColor == IndexedColors.AUTOMATIC.getIndex()
      // headerPoi.toList(1).getCellStyle.getFillForegroundColor == IndexedColors.AUTOMATIC.getIndex()
      if (cell.getCellStyle.getFillForegroundColor == IndexedColors.AUTOMATIC.getIndex()) {
        true
      } else {
        val color = cell.getCellStyle.getFillForegroundColorColor.asInstanceOf[XSSFColor]
        color.getRGB.deep == Array(-1, -1, -1).deep
      }
      // val color = cell.getCellStyle.getFillForegroundColorColor.asInstanceOf[XSSFColor]
      // color.
    } */ .map { cell =>
      formatter.formatCellValue(cell)
    }
    // // val cell = sheet.getRow(3).getCell(0); val style = cell.getCellStyle
    // // import org.apache.poi.xssf.usermodel.XSSFColor
    // // val color = cell.getCellStyle.getFillForegroundColorColor.asInstanceOf[XSSFColor]

    val creationHelper = workbook.getCreationHelper
    val evaluator = creationHelper.createFormulaEvaluator
    lazy val body: List[List[String]] = bodyPoi.map {row =>
      val patternNegative = """\(([.0-9])\)""".r
      row.iterator.toList /* .filter { cell =>
        if (cell.getCellStyle.getFillForegroundColor == IndexedColors.AUTOMATIC.getIndex()) {
          true
        } else {
          val color = cell.getCellStyle.getFillForegroundColorColor.asInstanceOf[XSSFColor]
          color.getRGB.deep == Array(-1, -1, -1).deep
        }
      } */ .map {cell =>
        cell.getCellType match {
          case Cell.CELL_TYPE_FORMULA => {
            // cell.getNumericCellValue.toString
            val cellResult = evaluator.evaluateInCell(cell)
            val s = formatter.formatCellValue(cellResult)
            val pattern = """^ *\(([0-9,]+)\) *$""".r
            s match {
              case pattern(num) => "-" + num.toString
              case s2: String => if (s2 == "---") "" else s2
            }
          }
          case _ => {
            val s = formatter.formatCellValue(cell)
            s
          }
        }
      }
    }
  }
  def apply(file: File): XlsxFile = {
    new XlsxFile(file)
  }
  def apply(filename: String): XlsxFile = {
    apply(new File(filename))
  }
}

object Util {
  def main(args: Array[String]): Unit = {
    val xlsx = XlsxFile("/Users/knaka/doc/2016/sakuragumi/dummy_data_2-mod.xlsx")
    val filename = "/Users/knaka/doc/2016/sakuragumi/dummy_data_2-mod.xlsx"
    val workbook = WorkbookFactory.create(new File(filename))
    val sheet = workbook.getSheetAt(0)
    val numsample = 3
    val header :: body = sheet.iterator.toList
    val samplerows = body.take(numsample)
    val colnames = header.iterator.toList.map({cell => cell.getStringCellValue})
    val sampletypes = samplerows.map({row =>
      row.toList.map({cell =>
        cell.getCellType match {
          case Cell.CELL_TYPE_BLANK => 'string
          case Cell.CELL_TYPE_BOOLEAN => 'boolean
          case Cell.CELL_TYPE_FORMULA => 'numeric
          case Cell.CELL_TYPE_NUMERIC => 'numeric
          case Cell.CELL_TYPE_STRING => 'string
          case _ => 'error
        }
      })
    })
    val numcol = header.iterator.toList.length
    val f: Boolean = sampletypes.tail.forall(e => e == sampletypes.head)
    val types = sampletypes.head
    // Create DDL
    val bld = new StringBuilder
    (0 to (numcol - 1)).foreach { i =>
      // val
      bld.append(s"col$i text\n")
    }
    // println(bld.result)
    val ddl = "CREATE TABLE foo (" + (0 to (numcol - 1)).map({ i =>
      // s"col${i} ${types(i)}"
      s"col${i} text"
    }).mkString(", ") + ")"
    // data
    val formatter = new DataFormatter()
    body.take(2).map { rowPoi =>
      val row = rowPoi.toList
      val l = (0 to (numcol - 1)).map { i =>
        val cell = row(i)
        val s: String = cell.getCellType match {
          case Cell.CELL_TYPE_BLANK => ""
          case Cell.CELL_TYPE_BOOLEAN => if (cell.getBooleanCellValue) "true" else "false"
          case Cell.CELL_TYPE_ERROR => ""
          case Cell.CELL_TYPE_FORMULA =>
            if (types(i) == 'string) {
              formatter.formatCellValue(cell)
              // cell.getStringCellValue()
            } else {
              formatter.formatCellValue(cell)
              // cell.getStringCellValue()
              // cell.getNumericCellValue.toString
            }
          case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue.toString
          case Cell.CELL_TYPE_STRING => cell.getStringCellValue
        }
        s
      }
      l
    }


    // samplerows(0).iterator.toList.map({cell =>
    //   (
    //     cell.getCellType match {
    //       case Cell.CELL_TYPE_BLANK => cell.getStringCellValue
    //       case Cell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue
    //       case Cell.CELL_TYPE_ERROR => cell.getErrorCellValue
    //       case Cell.CELL_TYPE_FORMULA => cell.getNumericCellValue + " (" + cell.getCellFormula + ")"
    //       case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue
    //       case Cell.CELL_TYPE_STRING => cell.getStringCellValue
    //     },
    //     cell.getCellType match {
    //       case Cell.CELL_TYPE_BLANK => "BLANK"
    //       case Cell.CELL_TYPE_BOOLEAN => "BOOLEAN"
    //       case Cell.CELL_TYPE_ERROR => "ERROR"
    //       case Cell.CELL_TYPE_FORMULA => "FORMULA"
    //       case Cell.CELL_TYPE_NUMERIC => "NUMERIC"
    //       case Cell.CELL_TYPE_STRING => "STRING"
    //     }
    //   )
    // })
  }
}

// import java.io.File
// import org.apache.poi.ss.usermodel._
// import org.apache.poi.xssf.usermodel.XSSFColor
// import org.apache.poi.ss.usermodel.IndexedColors
//
// import java.util.stream.Stream
// import java.nio.file.Files
// import java.nio.file.Paths
// import java.util.function._
// import java.nio.charset.StandardCharsets
// import java.io.IOException
//
// object Hello {
//   def main(args: Array[String]): Unit = {
//     val workbook = WorkbookFactory.create(new File("/home/knaka/dev/minimal-scala/datforpoi.xlsx"))
//     workbook.forEach(new Consumer[Sheet] {
//       def accept(sheet: Sheet) = {
//         val indent = ""
//         println(indent + "Sheet name: " + sheet.getSheetName)
//         sheet.forEach(new Consumer[Row] {
//           def accept(row: Row) = {
//             val indent = "  "
//             println(indent + "Row number: " + row.getRowNum)
//             row.forEach(new Consumer[Cell] {
//               def accept(cell: Cell) = {
//                 val indent = "      "
//                 println("    Column (Cell) index: " + cell.getColumnIndex)
//                 val value = cell.getCellType match {
//                   case Cell.CELL_TYPE_BLANK => cell.getStringCellValue
//                   case Cell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue
//                   case Cell.CELL_TYPE_ERROR => cell.getErrorCellValue
//                   case Cell.CELL_TYPE_FORMULA => cell.getStringCellValue + " (" + cell.getCellFormula + ")"
//                   case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue
//                   case Cell.CELL_TYPE_STRING => cell.getStringCellValue
//                 }
//                 println(indent + "Value: " + value)
//                 val color = cell.getCellStyle.getFillForegroundColor match {
//                   case 0 => cell.getCellStyle.getFillForegroundColorColor match {
//                     case color: XSSFColor => "XSSFColor " + color.getARGBHex
//                     case _ => "Unknwon"
//                   }
//                   case idx: Short => {
//                     IndexedColors.values.filter(idx == _.getIndex) match {
//                       case Array() => "Unknown"
//                       case Array(icol, _*) => "IndexedColors " + icol.name
//                     }
//                   }
//                   case _ => "Unknwon"
//                 }
//                 println(indent + "Color: " + color)
//               }
//             })
//           }
//         })
//       }
//     })
//     // //  Workbook (POI API Documentation) https://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/Workbook.html
//     // val workbook = WorkbookFactory.create(new File("/home/knaka/dev/minimal-scala/datforpoi.xlsx"))
//     // val sheetConsumer = new Consumer[Sheet] { def accept(sheet: Sheet) = println(sheet.getSheetName) }
//     // workbook.forEach(sheetConsumer)
//     // val sheet = workbook.getSheetAt(0)
//     // val row = sheet.getRow(0)
//     // val rowConsumer = new Consumer[Row] {
//     //   def accept(row: Row) = {
//     //     println("d0: " + row.getRowStyle)
//     //     println("d1: " + row.getLastCellNum)
//     //   }
//     // }
//     // sheet.forEach(rowConsumer)
//     // val cellConsumer = new Consumer[Cell] {
//     //   def accept(cell: Cell) = {
//     //     val value = cell.getCellType match {
//     //       case Cell.CELL_TYPE_BLANK => cell.getStringCellValue
//     //       case Cell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue
//     //       case Cell.CELL_TYPE_ERROR => cell.getErrorCellValue
//     //       // case Cell.CELL_TYPE_FORMULA => cell.getCellFormula
//     //       case Cell.CELL_TYPE_FORMULA => cell.getStringCellValue
//     //       case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue
//     //       case Cell.CELL_TYPE_STRING => cell.getStringCellValue
//     //     }
//     //     println("d (" + cell.getRowIndex + ", " + cell.getColumnIndex + "): " + value)
//     //   }
//     // }
//     // val cell = row.getCell(0)
//     // val rowConsumer2 = new Consumer[Row] {
//     //   def accept(row: Row) = {
//     //     row.forEach(cellConsumer)
//     //   }
//     // }
//     // sheet.forEach(rowConsumer2)
//     // // val cell = sheet.getRow(1).getCell(0); val style = cell.getCellStyle
//     // // val cell = sheet.getRow(2).getCell(0); val style = cell.getCellStyle
//
// // val cell = sheet.getRow(3).getCell(0); val style = cell.getCellStyle
// // import org.apache.poi.xssf.usermodel.XSSFColor
// // val color = cell.getCellStyle.getFillForegroundColorColor.asInstanceOf[XSSFColor]
//
//
//     // val row = sheet.getRow(0)
//     // val cell = row.getCell(0)
//     // val value = cell.getCellType match {
//     //   case Cell.CELL_TYPE_BLANK => cell.getStringCellValue
//     //   case Cell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue
//     //   case Cell.CELL_TYPE_ERROR => cell.getErrorCellValue
//     //   case Cell.CELL_TYPE_FORMULA => cell.getCellFormula
//     //   case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue
//     //   case Cell.CELL_TYPE_STRING => cell.getStringCellValue
//     // }
//     // println("Hello, world!")
//   }
// }
