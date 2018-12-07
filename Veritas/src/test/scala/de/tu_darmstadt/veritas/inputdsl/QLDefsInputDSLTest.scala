package de.tu_darmstadt.veritas.inputdsl

import java.io.PrintWriter

import de.tu_darmstadt.veritas.backend.util.prettyprint.PrettyPrintWriter

object QLDefsInputDSLTest {

  def main(args: Array[String]) {
    val outputPrettyPrinter = new PrettyPrintWriter(new PrintWriter(System.out))
    outputPrettyPrinter.writeln()
    outputPrettyPrinter.writeln("Here are the test modules:")
    QLDefs.BasicTypes.prettyPrint(outputPrettyPrinter)
    QLDefs.QLSyntax.prettyPrint(outputPrettyPrinter)
    QLDefs.QLSemanticsData.prettyPrint(outputPrettyPrinter)
    QLDefs.QLSemantics.prettyPrint(outputPrettyPrinter)
    QLDefs.QLTypeSystem.prettyPrint(outputPrettyPrinter)
    QLDefs.QLTypeSystemInv.prettyPrint(outputPrettyPrinter)
    outputPrettyPrinter.close()
  }

}
