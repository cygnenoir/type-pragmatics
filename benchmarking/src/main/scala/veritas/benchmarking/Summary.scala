package veritas.benchmarking

import java.io.File
import info.folone.scala.poi.Workbook
import veritas.benchmarking.Main.Config

import scala.collection.immutable.ListMap

case class FileSummary(proverConfig: ProverConfig, proverResult: ProverResult, timeSeconds: Double)

case class Summary(config: Config) {
  private var fileSummaries: ListMap[File, FileSummary] = ListMap()

  def +=(fileResult: (File, FileSummary)): Unit = {
    fileSummaries += fileResult
    if (config.logPerFile) {
      val file = fileResult._1
      val res = fileResult._2
      print(s"Prover ${res.proverConfig.name} finished $file in ${res.timeSeconds.formatted("%.3f")} seconds: ${res.proverResult.status}\n")
    }
  }
  def +=(file: File, fileResult: FileSummary): Unit = {
    += (file -> fileResult)
  }

  def makeSummary: String = {
    val grouped = fileSummaries.groupBy(_._2.proverConfig)

    val b = StringBuilder.newBuilder
    for (group <- grouped) {
      val proverConfig = group._1
      val files = group._2
      val numFiles = files.size
      val proved = files filter (_._2.proverResult match {case Proved(_) => true; case _ => false})
      val numProved = proved.size
      val sumProvedTimes = proved map (_._2.timeSeconds) reduce(_+_)
      val avgProvedTimeSeconds = sumProvedTimes / numProved

      b ++= s"Prover ${proverConfig.name} attempted $numFiles, proved $numProved, average proved time ${avgProvedTimeSeconds.formatted("%.3f")} seconds\n"
    }

    b.toString()
  }

  def makeCSV: String = {
    val sep = ","
    val b = StringBuilder.newBuilder
    def cell(s: String, end: Boolean = false) = {
      b ++= escape(s, sep, "\"")
      if (end) b ++= "\n"
      else b ++= sep
    }
    
    b ++= s"Prover$sep Timeout$sep File$sep Time-milliseconds$sep Status$sep Details\n"
    for ((file, res) <- fileSummaries) {
      cell(res.proverConfig.name)
      cell(config.timeout.toString)
      cell(file.getAbsolutePath)
      cell((res.timeSeconds * 1000).toString)
      cell(res.proverResult.status)
      cell(res.proverResult.details, true)
    }

    b.toString
  }

  def escape(s: String, avoid: String, quote: String): String = {
    if (!s.contains(avoid) && !s.contains("\n"))
      s.trim
    else
      quote + s.trim.replace("\"", "\\\"").replace("\n", "\t\t") + quote
  }


  def makeXLS: Workbook = {
    import info.folone.scala.poi._

    val grouped = fileSummaries.groupBy(_._2.proverConfig)

    var sheets = Set[Sheet]()
    for (group <- grouped) {
      val proverConfig = group._1
      val files = group._2

      val header = Row(0) { Set(
        StringCell(0, "Prover"),
        StringCell(1, "Timeout"),
        StringCell(2, "File"),
        StringCell(3, "Time-ms"),
        StringCell(4, "Status"),
        StringCell(5, "Details")
      )}

      var rows = Set(header)
      var rowNum = 1
      for ((file, res) <- fileSummaries) {
        val cells = Set[Cell](
          StringCell(0, res.proverConfig.name),
          NumericCell(1, config.timeout),
          StringCell(2, file.getAbsolutePath),
          NumericCell(3, res.timeSeconds * 1000.0),
          StringCell(4, res.proverResult.status),
          StringCell(5, res.proverResult.details.replace("\n","\t").substring(0, Math.min(res.proverResult.details.length, 32767)))
        )
        rows += Row(rowNum) { cells }
        rowNum += 1
      }

      val sheet = Sheet(proverConfig.name) { rows }
      sheets += sheet
    }

    Workbook(sheets)
  }
}