package system

import system.Syntax.{ISort, Rule, Sort, Symbol}

import scala.collection.immutable.ListMap

case class Language(name: String, sorts: Seq[_ <: ISort], syms: Seq[Symbol], rules: Seq[Rule]) {
  override def toString: String = {
    s"""sorts
        |${sorts.mkString(", ")}
        |
         |symbols
        |${syms.map(_.sigString).mkString("\n")}
        |
         |rules
        |${rules.mkString("\n\n")}
       """.stripMargin
  }

  val closedDataTypes: ListMap[Sort, Seq[Symbol]] = {
    val types = sorts.flatMap(s => if (s.isInstanceOf[Sort] && !s.open) Some(s.asInstanceOf[Sort]) else None)
    ListMap() ++ types.map(s => s -> syms.filter(sym => sym.constr && sym.out == s))
  }

  val openDataTypes: Seq[Sort] =
    sorts.flatMap(s => if (s.isInstanceOf[Sort] && s.open) Some(s.asInstanceOf[Sort]) else None)

  val funSymbols: Seq[Symbol] = {
    val constrs = closedDataTypes.values.flatten.toSeq
    syms.diff(constrs)
  }
}
