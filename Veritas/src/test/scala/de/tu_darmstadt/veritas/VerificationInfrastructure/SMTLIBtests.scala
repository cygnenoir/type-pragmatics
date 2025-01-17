package de.tu_darmstadt.veritas.VerificationInfrastructure

import de.tu_darmstadt.veritas.VerificationInfrastructure.verifier.{ADTVampireVerifier, TPTPVampireVerifier, Verifier, Z3VampireVerifier}
import de.tu_darmstadt.veritas.backend.ast.{VeritasConstruct, VeritasFormula}
import org.scalatest.FunSuite


/**
  * Created by sylvia on 31.07.17.
  */
class SMTLIBtests extends FunSuite {

//  val SQLgraph = new SQLSoundnessProofGraphTest
//  import SQLgraph._
//
//  val vampire_tar = new ADTVampireVerifier()
//
//  val defaultshort_timeout = 5
//  val defaultlong_timeout = 120
//  val unsuccessful_timeout = 1
//
//  def makeCustomVampireTar(timeout: Int) = new ADTVampireVerifier(timeout)
//  def makeCustomVampireZ3(timeout: Int) = new Z3VampireVerifier(timeout)
//  def makeCustomVampire(timeout: Int) = new TPTPVampireVerifier(timeout)
//
//  def successfulVerification(ps: loaded_g.ProofStep,
//                             v: Verifier[VeritasConstruct, VeritasFormula]): Unit = {
//    val res = loaded_g.verifyProofStep(ps, v)
//    println(res.status) //for debugging
//    assert(res.status.isVerified)
//  }
//
//  def inconclusiveVerification(ps: loaded_g.ProofStep,
//                               v: Verifier[VeritasConstruct, VeritasFormula]): Unit = {
//    val res = loaded_g.verifyProofStep(ps, v)
//    println(res.status) //for debugging
//    assert(!res.status.isVerified)
//    assert(res.errorMsg.nonEmpty)
//    assert(res.errorMsg.get == "Time limit")
//  }
//
//  test("Successful verification of Tvalue base case") {
//    successfulVerification(tvaluecasePS, makeCustomVampire(defaultshort_timeout))
//    successfulVerification(tvaluecasePS, makeCustomVampireTar(defaultshort_timeout))
//    successfulVerification(tvaluecasePS, makeCustomVampireZ3(defaultshort_timeout))
//  }
//
//  test("Try verifying selectFromWhere case top-level (inconclusive)") {
//    inconclusiveVerification(selcasePS, makeCustomVampire(unsuccessful_timeout))
//    inconclusiveVerification(selcasePS, makeCustomVampireTar(unsuccessful_timeout))
//  }
//
//  test("Successful verification of top set goals (union)") {
//    successfulVerification(unioncasePS, makeCustomVampire(defaultshort_timeout))
//    successfulVerification(unioncasePS, makeCustomVampireTar(defaultshort_timeout))
//  }
//
//  test("Successful verification of top set goals (intersection)") {
//    successfulVerification(intersectioncasePS, makeCustomVampire(defaultshort_timeout))
//    successfulVerification(intersectioncasePS, makeCustomVampireTar(defaultshort_timeout))
//  }
//
//  test("Successful verification of top set goals (difference)") {
//    successfulVerification(intersectioncasePS, makeCustomVampire(defaultshort_timeout))
//    successfulVerification(differencecasePS, makeCustomVampireTar(defaultshort_timeout))
//  }
//
//  //difference to normal Vampire 4.1 using untyped logic....! (with typed logic, it also does not work)
//  test("Unsuccessful verification of union tvalue-tvalue") {
//    val ps = loaded_g.appliedStep(loaded_g.requiredObls(unioncasePS).head._1).get
//    inconclusiveVerification(ps, makeCustomVampire(defaultlong_timeout))
//    inconclusiveVerification(ps, makeCustomVampireTar(defaultlong_timeout))
//  }
//
//  test("Unsuccessful verification of intersection tvalue-tvalue") {
//    val ps = loaded_g.appliedStep(loaded_g.requiredObls(intersectioncasePS).head._1).get
//    inconclusiveVerification(ps, makeCustomVampire(defaultlong_timeout))
//    inconclusiveVerification(ps, makeCustomVampireTar(defaultlong_timeout))
//  }
//
//  test("Unsuccessful verification of difference tvalue-tvalue") {
//    val ps = loaded_g.appliedStep(loaded_g.requiredObls(differencecasePS).head._1).get
//    inconclusiveVerification(ps, makeCustomVampire(defaultlong_timeout))
//    inconclusiveVerification(ps, makeCustomVampireTar(defaultlong_timeout))
//  }
//
//  test("verification of more difficult set sub-cases") {
//    val unsuccessfulObls = loaded_g.requiredObls(unioncasePS).tail ++
//      loaded_g.requiredObls(intersectioncasePS).tail ++ loaded_g.requiredObls(differencecasePS).tail
//
//    for ((o, e) <- unsuccessfulObls) {
//      val stepO = loaded_g.appliedStep(o)
//      assert(stepO.nonEmpty)
//      val step = stepO.get
//      inconclusiveVerification(step, makeCustomVampire(defaultlong_timeout))
//      inconclusiveVerification(step, makeCustomVampireTar(defaultlong_timeout))
//
//    }
//  }


}
