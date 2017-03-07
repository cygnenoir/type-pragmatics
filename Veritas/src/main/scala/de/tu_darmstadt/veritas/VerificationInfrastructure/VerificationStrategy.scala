package de.tu_darmstadt.veritas.VerificationInfrastructure

/**
  * Strategies for labeling edges of ProofTrees
  */
abstract class VerificationStrategy[S, P] {
  def fullyVerified(edgeseq: Seq[(ProofEdgeLabel, Boolean)]): Boolean

  def callVerifier(verifier: Verifier[S, P], spec: S, goal: P, edges: Seq[(ProofEdgeLabel, ProofStep[S, P])]): VerificationStatus
}

/**
  * default strategy: simply try to figure out a proof for a node of a proof tree given its children, e.g. calling an ATP
  */

case class Solve[S, P]() extends VerificationStrategy[S, P] {
  override def fullyVerified(edgeseq: Seq[(ProofEdgeLabel, Boolean)]): Boolean = {
    edgeseq.forall { e => e._2 }
  }

  override def callVerifier(verifier: Verifier[S, P], spec: S, goal: P, edges: Seq[(ProofEdgeLabel, ProofStep[S, P])]): VerificationStatus = {
    val hypotheses = edges.map { e => e._2.goal }
    verifier.verify(spec, hypotheses, goal, this)
  }
}

// below is only a copy of Solve from before induction was refined
//case class Induction[S, P]() extends VerificationStrategy[S, P] {
//  override def fullyVerified(edgeseq: Seq[(ProofEdgeLabel, Boolean)]): Boolean = {
//    edgeseq.forall { e => e._2 }
//  }
//
//  override def callVerifier(verifier: Verifier[S, P], spec: S, goal: P, edges: Seq[(ProofEdgeLabel, ProofStep[S, P])]): VerificationStatus = {
//    val hypotheses = edges.map { e => (e._1, e._2.goal)}
//    verifier.verify(spec, hypotheses, goal, this)
//  }
//}

case class StructuralInduction[S, P](inductionvar: S) extends VerificationStrategy[S, P] {
  override def fullyVerified(edgeseq: Seq[(ProofEdgeLabel, Boolean)]): Boolean = {
    //ignore all edges that are not structural induction edges
    val inductioncases: Seq[(ProofEdgeLabel, Boolean)] =
      edgeseq.filter(e =>
        e._1 match {
          case StructInductCase(_, _) => true
          case _ => false
        })
    inductioncases.forall { e => e._2 }
  }

  override def callVerifier(verifier: Verifier[S, P], spec: S, goal: P, edges: Seq[(ProofEdgeLabel, ProofStep[S, P])]): VerificationStatus = {
    //ignore all edges that are not structural induction edges
    val inductioncases: Seq[(ProofEdgeLabel, ProofStep[S, P])] =
      edges.filter( (e : (ProofEdgeLabel, ProofStep[S, P])) =>
        e._1 match {
          case StructInductCase(_, _) => true
          case _ => false
        })
    val structindcases: Seq[(StructInductCase[P], ProofStep[S, P])] =
      inductioncases.map(e => e._1 match {
        case s : StructInductCase[P] => (s, e._2)
          // this would throw a match error if the edge label is still not a StructInductCase
      })
    val hypotheses = structindcases.flatMap { e => e._1.ihs }
    verifier.verify(spec, hypotheses, goal, this)

    //TODO we might have to refine the verifier call for induction once we really support this via a prover
  }
}

case class CaseDistinction[S, P]() extends VerificationStrategy[S, P] {
  override def fullyVerified(edgeseq: Seq[(ProofEdgeLabel, Boolean)]): Boolean = ???

  override def callVerifier(verifier: Verifier[S, P], spec: S, goal: P, edges: Seq[(ProofEdgeLabel, ProofStep[S, P])]): VerificationStatus = ???
}

//TODO which other abstract strategies are there for verifying proof trees?

case class VerificationConfiguration[S, P, V](
                                               transformer: Transformer[S, P, V],
                                               strat: VerificationStrategy[S, P],
                                               prover: Prover[V],
                                               usedEdges: Seq[(ProofEdgeLabel, ProofStep[S, P])],
                                               verifier: Verifier[S, P])
