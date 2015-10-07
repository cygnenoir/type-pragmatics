package de.tu_darmstadt.veritas.backend.transformation.defs

import de.tu_darmstadt.veritas.backend.veritas._
import de.tu_darmstadt.veritas.backend.transformation.ModuleTransformation
import de.tu_darmstadt.veritas.backend.transformation.lowlevel.CollectTypeInfo
import de.tu_darmstadt.veritas.backend.transformation.TransformationError
import de.tu_darmstadt.veritas.backend.util.FreshNames
import de.tu_darmstadt.veritas.backend.util.FreeVariables

trait CollectSubformulas extends ModuleTransformation {
  var freshNames = new FreshNames

  //collect the new meta variable names that are generated
  var generatedNames: Set[MetaVar] = Set()

  //collect the additional naming premises that are generated during traversal
  //var additionalPremises: Set[TypingRuleJudgment] = Set()
  var additionalPremises: Map[FunctionExpMeta, MetaVar] = Map()

  //collect premises generated in exists/forall bodies!
  var inneradditionalPremises: Map[FunctionExpMeta, MetaVar] = Map()
  /**
   * override to control which constructs get named subformulas
   *
   * can use information from path variable, for example
   * parameter vc is the construct itself
   */
  def checkConstruct(vc: VeritasConstruct): Boolean = true

  /**
   * gets subformula for which a meta variable is to be generated
   * as parameter
   *
   * this information can be used to construct a certain name
   * default implementation just generates a fresh name with "VAR"
   *
   */
  def newMetaVar(vc: VeritasConstruct): MetaVar = MetaVar(freshNames.freshName("VAR"))

  private def addAdditionalPremise(mv: MetaVar, mexp: FunctionExpMeta): Unit =
    additionalPremises = additionalPremises + (mexp -> mv)

  //FunctionExpJudgment(FunctionExpEq(FunctionMeta(mv), mexp))

  override def apply(m: Seq[Module]): Seq[Module] = {
    //make sure that any mutable state is initialized upon application!
    freshNames = new FreshNames
    generatedNames = Set()
    additionalPremises = Map()
    inneradditionalPremises = Map()
    super.apply(m)
  }

  override def transTypingRules(tr: TypingRule): Seq[TypingRule] = {
    //the traversal of the other constructs will collect named subformulas as premises
    //and modify variable additionalPremises
    //reset additionalPremises & freshNames for each new typing rule that is traversed
    additionalPremises = Map()
    freshNames = new FreshNames
    generatedNames = Set()
    tr match {
      case t @ TypingRule(n, prems, conss) => super.transTypingRules(t)
    }
  }

  override def transTypingRuleJudgment(trj: TypingRuleJudgment): TypingRuleJudgment =
    //warning: unclear if the current solution works for nested exists/forall
    //I think it should, but didn't test ...
    trj match {
      case e @ ExistsJudgment(vl, jl) => {
        val oldaddprems = additionalPremises
        additionalPremises = Map()
        val res = ExistsJudgment(trace(vl)(transMetaVars(_)), trace(jl)(transTypingRuleJudgments(_)))
        inneradditionalPremises = additionalPremises
        additionalPremises = oldaddprems
        res
      }
      case e @ ForallJudgment(vl, jl) => {
        val oldaddprems = additionalPremises
        additionalPremises = Map()
        val res = ForallJudgment(trace(vl)(transMetaVars(_)), trace(jl)(transTypingRuleJudgments(_)))
        inneradditionalPremises = additionalPremises
        additionalPremises = oldaddprems
        res
      }
      case t => super.transTypingRuleJudgment(t)
    }

  override def transTypingRuleJudgments(trj: TypingRuleJudgment): Seq[TypingRuleJudgment] =
    //warning: unclear if the current solution works for nested exists/forall
    //I think it should, but didn't test ...
    trj match {
      case e @ ExistsJudgment(vl, jl) => {
        val oldaddprems = additionalPremises
        additionalPremises = Map()
        val res = Seq(ExistsJudgment(trace(vl)(transMetaVars(_)), trace(jl)(transTypingRuleJudgments(_))))
        inneradditionalPremises = additionalPremises
        additionalPremises = oldaddprems
        res
      }
      case e @ ForallJudgment(vl, jl) => {
        val oldaddprems = additionalPremises
        additionalPremises = Map()
        val res = Seq(ForallJudgment(trace(vl)(transMetaVars(_)), trace(jl)(transTypingRuleJudgments(_))))
        inneradditionalPremises = additionalPremises
        additionalPremises = oldaddprems
        res
      }
      case t => super.transTypingRuleJudgments(t)
    }

  protected def findExistingPremise(mexp: FunctionExpMeta): Option[FunctionMeta] =
    if (additionalPremises contains mexp)
      Some(FunctionMeta(additionalPremises(mexp)))
    else None
  //  {
  //    def traversePremises(prems: Seq[TypingRuleJudgment]): Option[FunctionMeta] =
  //      prems match {
  //        case Seq() => None
  //        case p +: ps => p match {
  //          case FunctionExpJudgment(FunctionExpEq(fm @ FunctionMeta(_), r)) if (r == mexp) => Some(fm)
  //          case _ => traversePremises(ps)
  //        }
  //      }
  //
  //    traversePremises(additionalPremises.toSeq)
  //  }

  private def checkNew(mexp: FunctionExpMeta): FunctionExpMeta =
    if (checkConstruct(mexp))
      //check first whether there has already been a premise with the desired mexp
      //if so, don't add a new premise
      findExistingPremise(mexp) match {
        case Some(t) => mexp
        case None => mexp match {
          //only add a new premise if the given mexp is already
          //one of the generated meta variables
          case FunctionMeta(m) if (generatedNames contains m) => mexp
          //in all other cases, add a new premise
          case _ => {
            val newmeta = newMetaVar(mexp)
            generatedNames += newmeta
            addAdditionalPremise(newmeta, mexp)
            mexp
          }
        }
      }
    else mexp

  private def checkNews(mexp: FunctionExpMeta): Seq[FunctionExpMeta] =
    if (checkConstruct(mexp))
      //check first whether there has already been a premise with the desired mexp
      //if so, don't add a new premise
      findExistingPremise(mexp) match {
        case Some(fm) => Seq(mexp)
        case None => mexp match {
          //only add a new premise if the given mexp is already
          //one of the generated meta variables
          case FunctionMeta(m) if (generatedNames contains m) => Seq(mexp)
          //in all other cases, add a new premise
          case _ => {
            val newmeta = newMetaVar(path.head)
            generatedNames += newmeta
            addAdditionalPremise(newmeta, mexp)
            Seq(mexp)
          }
        }
      }
    else Seq(mexp)

  override def transFunctionExpMeta(f: FunctionExpMeta): FunctionExpMeta =
    withSuper(super.transFunctionExpMeta(f)) {
      case fmv @ FunctionMeta(mv) => checkNew(fmv)
    }

  override def transFunctionExpMetas(f: FunctionExpMeta): Seq[FunctionExpMeta] =
    withSuper(super.transFunctionExpMetas(f)) {
      case fmv @ FunctionMeta(mv) => checkNews(fmv)
    }

  override def transFunctionExps(f: FunctionExp): Seq[FunctionExp] =
    withSuper(super.transFunctionExps(f)) {
      case fe @ FunctionExpEq(f1, f2)    => Seq(FunctionExpEq(checkNew(f1), checkNew(f2)))
      case fe @ FunctionExpNeq(f1, f2)   => Seq(FunctionExpNeq(checkNew(f1), checkNew(f2)))
      case fe @ FunctionExpIf(c, t, e)   => Seq(FunctionExpIf(checkNew(c), checkNew(t), checkNew(e)))
      case fe @ FunctionExpLet(n, e, i)  => Seq(FunctionExpLet(n, checkNew(e), checkNew(i)))
      case fe @ FunctionExpApp(fn, args) => Seq(FunctionExpApp(fn, args map checkNew))
    }

  override def transFunctionExp(f: FunctionExp): FunctionExp =
    //note: naming of subformulas can anyway only ever be applied within constructs 
    //that accept FunctionExpMeta as arguments
    //(since substitution potentially replaces formula with MetaVar!)
    withSuper(super.transFunctionExp(f)) {
      case fe @ FunctionExpEq(f1, f2)    => FunctionExpEq(checkNew(f1), checkNew(f2))
      case fe @ FunctionExpNeq(f1, f2)   => FunctionExpNeq(checkNew(f1), checkNew(f2))
      case fe @ FunctionExpIf(c, t, e)   => FunctionExpIf(checkNew(c), checkNew(t), checkNew(e))
      case fe @ FunctionExpLet(n, e, i)  => FunctionExpLet(n, checkNew(e), checkNew(i))
      case fe @ FunctionExpApp(fn, args) => FunctionExpApp(fn, args map checkNew)
    }
}

/**
 * introduce variable names for subformulas for which checkConstruct holds
 *
 */
trait NameSubformulas extends ModuleTransformation with CollectSubformulas {

  /**
   * override to control which constructs actually get substituted
   * (if this shall differ from the constructs for which premises are generated)
   *
   * can use information from path variable, for example
   * parameter vc is the construct itself
   */
  def checkSubstitute(vc: VeritasConstruct): Boolean = true

  private def printAdditionalPremises(ap: Map[FunctionExpMeta, MetaVar]): Seq[TypingRuleJudgment] =
    for ((mexp, mv) <- ap.iterator.toSeq)
      yield FunctionExpJudgment(FunctionExpEq(FunctionMeta(mv), mexp))

  override def transTypingRules(tr: TypingRule): Seq[TypingRule] = {
    withSuper(super.transTypingRules(tr)) {
      case TypingRule(n, prems, conss) => {
        //the traversal of the other constructs will collect named subformulas as premises
        //and modify variable additionalPremises
        Seq(TypingRule(n, printAdditionalPremises(additionalPremises) ++ prems, conss))
      }
    }
  }

  private def makeOrImpl(jl: Seq[TypingRuleJudgment]): Seq[TypingRuleJudgment] = {
    val premisesSeq =
      (printAdditionalPremises(inneradditionalPremises) map (a => NotJudgment(a))) map (s => Seq(s))
    val orcases = premisesSeq :+ jl
    if (orcases.length > 1)
      Seq(OrJudgment(premisesSeq :+ jl))
    else
      jl
  }

  override def transTypingRuleJudgment(trj: TypingRuleJudgment): TypingRuleJudgment =
    withSuper(super.transTypingRuleJudgment(trj)) {
      case e @ ExistsJudgment(vl, jl) => {
        //note: jl already is the substituted body, so the quantified variables have to be recomputed
        val newexistsbody = makeOrImpl(jl)
        val allFV = FreeVariables.freeVariables(newexistsbody)
        //including generatedNames from allFV is ok since no generated name
        //within body jl can ever come from outside e (additionalPremises is reset when moving into e,
        //so no old names are retained!)
        val outsideVars = allFV diff generatedNames diff vl.toSet
        ExistsJudgment((allFV diff outsideVars).toSeq, newexistsbody)
      }
      case e @ ForallJudgment(vl, jl) => {
        //note: jl already is the substituted body, so the quantified variables have to be recomputed
        val newforallbody = makeOrImpl(jl)
        val allFV = FreeVariables.freeVariables(newforallbody)
        //including generatedNames from allFV is ok since no generated name
        //within body jl can ever come from outside e (additionalPremises is reset when moving into e,
        //so no old names are retained!)
        val outsideVars = allFV diff generatedNames diff vl.toSet
        ForallJudgment((allFV diff outsideVars).toSeq, newforallbody)
      }
    }

  override def transTypingRuleJudgments(trj: TypingRuleJudgment): Seq[TypingRuleJudgment] =
    withSuper(super.transTypingRuleJudgments(trj)) {
      case e @ ExistsJudgment(vl, jl) => {
        //note: jl already is the substituted body, so the quantified variables have to be recomputed
        val newexistsbody = makeOrImpl(jl)
        val allFV = FreeVariables.freeVariables(newexistsbody)
        //including generatedNames from allFV is ok since no generated name
        //within body jl can ever come from outside e (additionalPremises is reset when moving into e,
        //so no old names are retained!)
        val outsideVars = allFV diff generatedNames diff vl.toSet
        Seq(ExistsJudgment((allFV diff outsideVars).toSeq, newexistsbody))
      }
      case e @ ForallJudgment(vl, jl) => {
        //note: jl already is the substituted body, so the quantified variables have to be recomputed
        val newforallbody = makeOrImpl(jl)
        val allFV = FreeVariables.freeVariables(newforallbody)
        //including generatedNames from allFV is ok since no generated name
        //within body jl can ever come from outside e (additionalPremises is reset when moving into e,
        //so no old names are retained!)
        val outsideVars = allFV diff generatedNames diff vl.toSet
        Seq(ForallJudgment((allFV diff outsideVars).toSeq, newforallbody))
      }
    }

  private def checkAndSubstituteMexp(mexp: FunctionExpMeta): FunctionExpMeta =
    if (checkSubstitute(mexp))
      //check whether there has already been a premise with the desired mexp
      findExistingPremise(mexp) match {
        case Some(fm) => fm
        case None     => mexp
      }
    else mexp

  private def checkAndSubstituteMexps(mexp: FunctionExpMeta): Seq[FunctionExpMeta] =
    if (checkSubstitute(mexp))
      //check whether there has already been a premise with the desired mexp
      findExistingPremise(mexp) match {
        case Some(fm) => Seq(fm)
        case None     => Seq(mexp)
      }
    else Seq(mexp)

  override def transFunctionExpMeta(f: FunctionExpMeta): FunctionExpMeta =
    withSuper(super.transFunctionExpMeta(f)) {
      case fmv @ FunctionMeta(mv) => checkAndSubstituteMexp(fmv)
    }

  override def transFunctionExpMetas(f: FunctionExpMeta): Seq[FunctionExpMeta] =
    withSuper(super.transFunctionExpMetas(f)) {
      case fmv @ FunctionMeta(mv) => checkAndSubstituteMexps(fmv)
    }

  override def transFunctionExps(f: FunctionExp): Seq[FunctionExp] =
    withSuper(super.transFunctionExps(f)) {
      case fe @ FunctionExpEq(f1, f2)    => Seq(FunctionExpEq(checkAndSubstituteMexp(f1), checkAndSubstituteMexp(f2)))
      case fe @ FunctionExpNeq(f1, f2)   => Seq(FunctionExpNeq(checkAndSubstituteMexp(f1), checkAndSubstituteMexp(f2)))
      case fe @ FunctionExpIf(c, t, e)   => Seq(FunctionExpIf(checkAndSubstituteMexp(c), checkAndSubstituteMexp(t), checkAndSubstituteMexp(e)))
      case fe @ FunctionExpLet(n, e, i)  => Seq(FunctionExpLet(n, checkAndSubstituteMexp(e), checkAndSubstituteMexp(i)))
      case fe @ FunctionExpApp(fn, args) => Seq(FunctionExpApp(fn, args map checkAndSubstituteMexp))
    }

  override def transFunctionExp(f: FunctionExp): FunctionExp =
    //note: naming of subformulas can anyway only ever be applied within constructs 
    //that accept FunctionExpMeta as arguments
    //(since substitution potentially replaces formula with MetaVar!)
    withSuper(super.transFunctionExp(f)) {
      case fe @ FunctionExpEq(f1, f2)    => FunctionExpEq(checkAndSubstituteMexp(f1), checkAndSubstituteMexp(f2))
      case fe @ FunctionExpNeq(f1, f2)   => FunctionExpNeq(checkAndSubstituteMexp(f1), checkAndSubstituteMexp(f2))
      case fe @ FunctionExpIf(c, t, e)   => FunctionExpIf(checkAndSubstituteMexp(c), checkAndSubstituteMexp(t), checkAndSubstituteMexp(e))
      case fe @ FunctionExpLet(n, e, i)  => FunctionExpLet(n, checkAndSubstituteMexp(e), checkAndSubstituteMexp(i))
      case fe @ FunctionExpApp(fn, args) => FunctionExpApp(fn, args map checkAndSubstituteMexp)
    }

}

object NameEverything extends NameSubformulas

object NameEverythingSubstituteNothing extends NameSubformulas {
  override def checkSubstitute(vc: VeritasConstruct): Boolean = false
}

/**
 * excludes all meta variables from being named
 */
object NameEverythingButMetaVars extends NameSubformulas {
  override def checkConstruct(vc: VeritasConstruct): Boolean =
    vc match {
      case FunctionMeta(_) => false
      case MetaVar(_)      => false
      case _               => true
    }
}

object NameEverythingButMetaVarsSubstituteNothing extends NameSubformulas {
  override def checkConstruct(vc: VeritasConstruct): Boolean =
    vc match {
      case FunctionMeta(_) => false
      case MetaVar(_)      => false
      case _               => true
    }

  override def checkSubstitute(vc: VeritasConstruct): Boolean = false
}

/**
 * names for non-boolean function results only (RESULT-variable)
 */
object NameFunctionResultsOnly extends NameSubformulas {
  override def checkConstruct(vc: VeritasConstruct): Boolean = {
    //only rename right-hand side of single equation in conclusion
    val grandgrandparent = path(2)

    grandgrandparent match {
      case TypingRule(n, prems, Seq(FunctionExpJudgment(FunctionExpEq(l, r)))) if (r == vc) => true
      case _ => false
    }
  }

  override def newMetaVar(parent: VeritasConstruct): MetaVar = MetaVar("RESULT")

}

object NameSubstituteFunctionDefParametersOnly extends NameSubformulas with CollectTypeInfo {
  override def checkConstruct(vc: VeritasConstruct): Boolean = {
    //only rename in left-hand side of single equation in conclusion!
    val parent = path(0)
    val grandparent = path(1)

    def sidecondition(params: Seq[FunctionExpMeta]): Boolean =
      (params contains vc) &&
        (grandparent match {
          case FunctionExpEq(l, r)     => !(parent == r)
          case FunctionExpBiImpl(l, r) => !(parent == r)
          case _                       => false
        })

    if (path isDefinedAt 2) {
      val grandgrandparent = path(2)
      grandgrandparent match {
        case TypingRule(n, prems, Seq(FunctionExpJudgment(FunctionExpApp(f, args)))) if (args contains vc) => true
        case _ => if (path isDefinedAt 3) {
          val grandgrandgrandparent = path(3)
          grandgrandgrandparent match {
            case TypingRule(n, prems, Seq(FunctionExpJudgment(FunctionExpNot(FunctionExpApp(f, args))))) if (args contains vc) => true
            case TypingRule(n, prems, Seq(FunctionExpJudgment(FunctionExpEq(FunctionExpApp(f, args), r)))) if (sidecondition(args)) => true
            case TypingRule(n, prems, Seq(FunctionExpJudgment(FunctionExpBiImpl(FunctionExpApp(f, args), r)))) if (sidecondition(args)) => true
            case _ => false
          }
        } else false
      }
    } else false
  }

  override def checkSubstitute(vc: VeritasConstruct): Boolean = checkConstruct(vc)

  override def newMetaVar(vc: VeritasConstruct): MetaVar =
    path.head match {
      case FunctionExpApp(n, args) => {
        val vcpos = args.indexOf(vc)
        val funcparamsorts = if (functypes.isDefinedAt(n)) functypes(n)._1 else pfunctypes(n)._1
        MetaVar(freshNames.freshName(funcparamsorts(vcpos).name))
      }
      case _ => super.newMetaVar(vc) //should not happen
    }

}

object OldFunctionEqTransformation extends ModuleTransformation {
  override def apply(m: Seq[Module]): Seq[Module] = {
    NameSubstituteFunctionDefParametersOnly(NameFunctionResultsOnly(m))
  }
}




