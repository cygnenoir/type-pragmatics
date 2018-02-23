package de.tu_darmstadt.veritas.newinputdsl.dsk

import de.tu_darmstadt.veritas.backend.ast._
import de.tu_darmstadt.veritas.backend.ast.function.{FunctionDef, FunctionEq}

trait DomainSpecificKnowledge {

  def attachedProperties: Map[(FunctionDef, String), TypingRule]
  def propertiesNeeded: Map[TypingRule, Seq[FunctionEq]]
  def recursiveFunctions: Map[FunctionDef, DataType]
  def groupings: Seq[Seq[FunctionEq]]
  def distinctionsBasedOnExpressions: Map[FunctionEq, Seq[FunctionExpJudgment]]
  // A toplevel property is a property that is not needed by any other function
  def toplevelProperties: Map[(FunctionDef, String), TypingRule] = attachedProperties.filter { case ((fdef, propName), prop) =>
      propertiesNeeded.keys.forall( _ != prop)
  }
}