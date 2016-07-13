package controllers.excel

import java.io.File
import org.apache.poi.ss.usermodel._
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.ss.usermodel.IndexedColors
import collection.JavaConversions._

object Util {
  def main(args: Array[String]): Unit = {
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
          case _ => "ERROR"
        }
      })
    })
    val numcol = header.iterator.toList.length
    val f: Boolean = sampletypes.tail.forall(e => e == sampletypes.head)
    val types = sampletypes.head
    // DDL
    (0 to (numcol - 1)).foreach { i =>
      println(s"  col${i} ${types(i)}")
    }
    // data
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
              cell.getStringCellValue()
            } else {
              cell.getStringCellValue()
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