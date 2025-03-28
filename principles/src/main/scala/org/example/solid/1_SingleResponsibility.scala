package org.example.solid

// A class should have only one responsibility
object `1_SingleResponsibility` extends App:

  // Bad example
  // ReportData is responsible for both data and formatting
  // case class ReportData(lines: List[(Int, String, BigDecimal)]) {
  //   def print(): Unit =
  //     val formatted = lines.mkString("\n")
  //     Console.println(formatted)
  //   }
  // }

  // Only holds data
  case class ReportData(lines: List[(Int, String, BigDecimal)])

  // Only formats data
  trait ReportFormatter:
    def format(data: ReportData): String

  class HtmlReportFormatter extends ReportFormatter:
    def format(data: ReportData): String =
      val content = data.lines
        .map { case (id, name, value) =>
          s"<li>$name - $value</li>"
        }
        .mkString("")
      s"<ul>$content</ul>"

  // Only prints data
  trait Printer:
    def print(str: String): Unit

  class ConsolePrinter extends Printer:
    def print(str: String): Unit = println(str)

  val report = ReportData(
    List(
      (1, "Product 1", 100),
      (2, "Product 2", 200),
      (3, "Product 3", 300)
    )
  )

  val formatter = new HtmlReportFormatter
  val printer   = new ConsolePrinter

  printer.print(formatter.format(report))
