package ch.scorpion.jabbah.base.math

/**
 * A common abstraction for solving linear equation systems.
 *
 * Currently only implemented on the JVM platform. Once Kotlin multiplatform
 * implementations are available, this interface won't be needed any more
 */
interface LinearEquationSystemSolver {

	/**
	 * Solves the linear equation system defined by the coefficients matrix and the right-side constants.
	 * @return the result vector
	 */
	fun solve(system: LinearEquationSystem): DoubleArray
}

object UndefinedLinearEquationSystemSolver : LinearEquationSystemSolver {

	override fun solve(system: LinearEquationSystem): DoubleArray {
		throw UnsupportedOperationException("not implemented")
	}
}