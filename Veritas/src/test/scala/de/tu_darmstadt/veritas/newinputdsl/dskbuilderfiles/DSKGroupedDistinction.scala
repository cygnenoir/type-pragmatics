package de.tu_darmstadt.veritas.newinputdsl.dskbuilderfiles

import de.tu_darmstadt.veritas.newinputdsl.lang.SPLSpecification

object DSKGroupedDistinction extends SPLSpecification {
  trait Num extends Expression
  case class zero() extends Num
  case class succ(n: Num) extends Num

  def pred(n: Num): Num = n match {
    case zero() => zero()
    case succ(n) => n
  }

  @GroupedDistinction(Seq(0), Seq(1,2))
  @GroupedDistinction(Seq(0,2))
  @GroupedDistinction(Seq(1), Seq(2), Seq(0))
  def predpred(n: Num): Num = n match {
    case zero() => zero()
    case succ(zero()) => zero()
    case succ(succ(n)) => n
  }

  def plus(a: Num, b: Num): Num = (a, b) match {
    case (zero(), b) => if (b == zero() && b == b || a != zero()) zero() else succ(b)
    case (a, succ(n)) => succ(plus(a, n))
  }

  trait YN extends Expression
  case class yes() extends YN
  case class no() extends YN

  def multiplelets(a: Num, b: YN): YN = (a, b) match {
    case (zero(), yes()) =>
      val x = plus(zero(), succ(zero()))
      val y = plus(zero(), succ(zero()))
      val z = plus(zero(), succ(zero()))
      if ((x == zero()) <==> (y == zero())) yes() else no()
  }
}
