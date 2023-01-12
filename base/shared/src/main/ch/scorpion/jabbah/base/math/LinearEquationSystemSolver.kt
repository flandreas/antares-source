package ch.scorpion.jabbah.base.math

/**
 * A common abstraction for solving linear equation systems.
 *
 * Currently only implemented on the JVM platform. Once Kotlin multiplatform
 * implementations are available, this interface won't be needed any more
 */
interface LinearEquationSystemSolver {

	/**
	 * Solves the linear equation system defined by the coefficients matrix.
	 * @return the result vector
	 */
	fun solve(coefficients: Array<DoubleArray>, constants: DoubleArray): DoubleArray
}

object UndefinedLinearEquationSystemSolver : LinearEquationSystemSolver {

	override fun solve(coefficients: Array<DoubleArray>, constants: DoubleArray): DoubleArray {
		throw UnsupportedOperationException("not implemented")
	}
}