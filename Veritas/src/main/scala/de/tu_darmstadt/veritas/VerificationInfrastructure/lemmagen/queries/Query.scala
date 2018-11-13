package de.tu_darmstadt.veritas.VerificationInfrastructure.lemmagen.queries

import de.tu_darmstadt.veritas.VerificationInfrastructure.lemmagen.LemmaGenSpecEnquirer
import de.tu_darmstadt.veritas.backend.ast.SortRef
import de.tu_darmstadt.veritas.backend.ast.function.FunctionDef

object Query {
  implicit class QueryFunctionDef(val fctn: FunctionDef)(implicit enquirer: LemmaGenSpecEnquirer) {
    def name: String = fctn.signature.name
    def outType: SortRef = fctn.signature.out
    def inTypes: Seq[SortRef] = fctn.signature.in

    def successfulOutType: SortRef = {
      if(isFailable)
        enquirer.retrieveFailableConstructors(outType)._2.in.head
      else
        outType
    }

    def isDynamic: Boolean = enquirer.dynamicFunctions.contains(fctn)
    def isStatic: Boolean = enquirer.staticFunctions.contains(fctn)
    def isFailable: Boolean = enquirer.isFailableType(outType)
    def expects(t: SortRef): Boolean = inTypes.contains(t)
    def returns(t: SortRef): Boolean = outType == t
  }
}
