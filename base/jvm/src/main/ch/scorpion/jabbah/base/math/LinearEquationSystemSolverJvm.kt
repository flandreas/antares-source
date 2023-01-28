package ch.scorpion.jabbah.base.math

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.LUDecomposition

/**
 * Implements [LinearEquationSystemSolver] using the linear algebra library in [org.apache.commons.math3].
 */
object LinearEquationSystemSolverJvm : LinearEquationSystemSolver {

	override fun isNonSingular(system: LinearEquationSystem): Boolean {
		return LUDecomposition(createMatrix(system)).solver.isNonSingular
	}

	override fun solve(system: LinearEquationSystem): DoubleArray =
		LUDecomposition(createMatrix(system)).solver.solve(ArrayRealVector(system.getConstants().toDoubleArray(), false)).toArray()



	private fun createMatrix(system: LinearEquationSystem): Array2DRowRealMatrix =
		Array2DRowRealMatrix(system.getCoefficients().toTypedArray())
}