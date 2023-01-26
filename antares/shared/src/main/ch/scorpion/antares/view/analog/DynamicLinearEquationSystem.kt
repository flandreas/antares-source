package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.base.math.LinearEquationSystem

typealias DoubleSupplier = () -> Double

/**
 * A linear equation system whose coefficients and constants are dynamic,
 * i.e. they can be fetched from other objects before solving the system.
 */
class DynamicLinearEquationSystem(val numberOfVariables: Int) {

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

	fun toLinearEquationSystem(): LinearEquationSystem {
		val system = LinearEquationSystem(numberOfVariables)
		coefficients.forEachIndexed { i, c ->
			system.addEquation(c.map { it.invoke() }.toDoubleArray(), constants[i].invoke())
		}
		return system
	}

	fun getCoefficients(index: Int): DoubleArray =
		coefficients[index].map { it.invoke() }.toDoubleArray()

	fun getConstants(): DoubleArray =
		constants.map { it.invoke() }.toDoubleArray()
}