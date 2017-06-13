package de.tu_darmstadt.veritas.sudoku.tactics
import de.tu_darmstadt.veritas.VerificationInfrastructure.tactic.TacticApplicationException
import de.tu_darmstadt.veritas.VerificationInfrastructure.{EdgeLabel, GenObligation, ObligationProducer}
import de.tu_darmstadt.veritas.sudoku.{EmptySpec, IndexedSudokuUnit, SudokuField}

/**
  * Tactic for solving situations where
  * there is only one cell within a unit that contains this candidate
  *
  * assumes that candidates have been ruled out correctly
  */
object OnlyCellWithCandidate extends SudokuTactic {
  /**
    * applying a tactic to a ProofStep returns the edges generated from this application
    * edges include edge labels and sub-ProofSteps
    * caller has to decide whether the edges will be integrated into a proof graph or not
    *
    * @param obl
    * @param obllabels labels from edges that lead to the given obligation (for propagating proof info if necessary)
    * @throws TacticApplicationException
    * @return
    */
  override def apply[Obligation](obl: GenObligation[EmptySpec, SudokuField],
                                 obllabels: Iterable[EdgeLabel],
                                 produce: ObligationProducer[EmptySpec, SudokuField, Obligation]): Iterable[(Obligation, EdgeLabel)] = {
    val sudokuField = obl.goal
    val units: IndexedSudokuUnit = sudokuField.indexedRows ++ sudokuField.indexedColumns ++ sudokuField.indexedBoxes
    ???
  }
}