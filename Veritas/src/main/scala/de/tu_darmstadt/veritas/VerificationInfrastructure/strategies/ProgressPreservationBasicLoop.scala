package de.tu_darmstadt.veritas.VerificationInfrastructure.strategies

import de.tu_darmstadt.veritas.VerificationInfrastructure.tactics.LemmaApplication
import de.tu_darmstadt.veritas.VerificationInfrastructure.{ProofGraph, ProofGraphTraversals}
import de.tu_darmstadt.veritas.scalaspl.util.AugmentedCallGraph

/**
  * iterative strategy - builds a proof graph according to the "template" given by the augmented call graph
  * assumes a proof graph that already contains nodes (at least root nodes)
  * @param dsk domain-specific knowledge for the given specification
  * @param acg_gen function that can generate an augmented call graph for a given function name
  */
case class ProgressPreservationBasicLoop[Spec, Goal, Type, FDef, Prop, Equation, Criteria, Expression](override val dsk: DomainSpecificKnowledge[Type, FDef, Prop],
                                                                                                       override val acg_gen: String => AugmentedCallGraph[Equation, Criteria, Expression],
                                                                                                       retrievePropFromGoal: Goal => Prop)
  extends DomainSpecificStrategy[Spec, Goal, Type, FDef, Prop, Equation, Criteria, Expression] {


  // given obligation is the obligation to which the basic loop shall be applied
  override def applyToPG(pg: ProofGraph[Spec, Goal] with ProofGraphTraversals[Spec, Goal])(obl: pg.Obligation):
    ProofGraph[Spec, Goal] with ProofGraphTraversals[Spec, Goal] = {



    //step 1: retrieve relevant function name from given obligation with the help of the given domain specific knowledge dsk
    val goal = obl.goal
    val prop = retrievePropFromGoal(goal)

    val retrievefdef = findFDefForDSKProp(prop)

    val fname = retrievefdef match {
      case Some(fdef) => dsk.retrieveFunName(fdef)
      case None => sys.error(s"Basic Loop for progress/preservation proof generation could not retrieve a corresponding function for given goal $obl")
    }

    //step 2: create augmented call graph for relevant function
    val acg = acg_gen(fname)

    //step 3: apply strategy that traverses the computed ACG and grows the given proof graph accordingly
    GenerateSubgraphForSingleFunction(dsk, acg_gen, acg)


    //step 4: Collect all leaves that contain auxiliary lemmas (i.e. are children of a lemma application)
    val all_leaves = pg.leaves(Set(obl))

    def isLemmaApplicationLeaf(l: pg.Obligation): Boolean = {
      val maybe_lemtac = pg.requiringSteps(l).find { case (ps, _) => ps.tactic.isInstanceOf[LemmaApplication] }
      maybe_lemtac.nonEmpty
    }

    val pp_leaves: Seq[pg.Obligation] = for (l <- all_leaves if isLemmaApplicationLeaf(l)) yield l

    //step 5: recall the basic loop for all leaves collected above
    for (l <- pp_leaves) this.applyToPG(pg)(l)

    //final step: apply Solve tactic to all leaves
    //ApplySolveToLeaves().applyToPG(pg)(obl)


    pg
  }

  private def findFDefForDSKProp(p: Prop): Option[FDef] =
    (dsk.preservationProperties ++ dsk.progressProperties).find(pair => pair._2.contains(p)).map(_._1)
}
