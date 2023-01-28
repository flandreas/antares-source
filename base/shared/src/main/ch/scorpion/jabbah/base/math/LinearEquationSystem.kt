package ch.scorpion.jabbah.base.math

import ch.scorpion.jabbah.base.module.BaseModule

class LinearEquationSystem(val numberOfVariables: Int) {

	private val coefficients = mutableListOf<DoubleArray>()

	private val constants = mutableListOf<Double>()

	val equationCount: Int get() = coefficients.size

	val isNonSingular: Boolean get() = BaseModule.linearEquationSystemSolver.isNonSingular(this)

	fun addEquation(coefficients: DoubleArray, constant: Double) {
		this.coefficients.add(coefficients)
		this.constants.add(constant)

	}

	fun getCoefficients(): List<DoubleArray> = coefficients

	fun getCoefficients(index: Int): DoubleArray = coefficients[index]

	fun getConstants(): List<Double> = constants

}