package ch.scorpion.jabbah.base.math

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.LUDecomposition

/**
 * Implements [LinearEquationSystemSolver] using the linear algebra library in [org.apache.commons.math3].
 */
object LinearEquationSystemSolverJvm : LinearEquationSystemSolver {

	override fun solve(coefficients: Array<DoubleArray>, constants: DoubleArray): DoubleArray =
		LUDecomposition(Array2DRowRealMatrix(coefficients)).solver.solve(ArrayRealVector(constants, false)).toArray()
}