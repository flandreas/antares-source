package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.base.math.LinearEquationSystem

typealias DoubleSupplier = () -> Double

/**
 * A linear equation system whose coefficients and constants are dynamic,
 * i.e. they can be fetched from other objects before solving the system.
 */
class DynamicLinearEquationSystem(val variableCount: Int) {

	companion object {
		val ZERO: DoubleSupplier = { 0.0 }
		val ONE: DoubleSupplier = { 1.0 }
		val MINUS_ONE: DoubleSupplier = { -1.0 }
	}

	private val coefficients = mutableListOf<Array<DoubleSupplier>>()

	private val constants = mutableListOf<DoubleSupplier>()

	val equationCount: Int get() = coefficients.size

	fun addEquation(coefficients: Array<DoubleSupplier>, constant: DoubleSupplier) {
		this.coefficients.add(coefficients)
		this.constants.add(constant)
	}

	private fun removeEquation(index: Int) {
		coefficients.removeAt(index)
		constants.removeAt(index)
	}

	fun toLinearEquationSystem(): LinearEquationSystem =
		toLinearEquationSystemImpl()

	private fun toLinearEquationSystemImpl(ignoredIndex: Int? = null): LinearEquationSystem {
		val system = LinearEquationSystem(variableCount)
		coefficients.forEachIndexed { i, c ->
			if (ignoredIndex == null || i != ignoredIndex) {
				system.addEquation(c.map { it.invoke() }.toDoubleArray(), constants[i].invoke())
			}
		}
		return system
	}

	fun getCoefficients(index: Int): DoubleArray =
		coefficients[index].map { it.invoke() }.toDoubleArray()

	fun getConstants(): DoubleArray =
		constants.map { it.invoke() }.toDoubleArray()

	fun removeLinearlyDependentEquation(range: IntRange) {
		if (variableCount >= equationCount) {
			throw IllegalStateException("Cannot remove equation from {$variableCount}x${equationCount} system")
		}
		if (equationCount > variableCount + 1) {
			throw IllegalStateException("Can only remove at most 1 equation")
		}

		range.forEach { ignoredIndex ->
			if (toLinearEquationSystemImpl(ignoredIndex).isNonSingular) {
				removeEquation(ignoredIndex)
				return
			}
		}

		throw IllegalStateException("Cannot make system linearly independent by removing equation")
	}
}