package ch.scorpion.jabbah.base.math

class LinearEquationSystem(val numberOfVariables: Int) {

	private val coefficients = mutableListOf<DoubleArray>()

	private val constants = mutableListOf<Double>()

	val equationCount: Int get() = coefficients.size

	fun addEquation(coefficients: DoubleArray, constant: Double) {
		this.coefficients.add(coefficients)
		this.constants.add(constant)
	}

	fun getCoefficients(): List<DoubleArray> = coefficients

	fun getCoefficients(index: Int): DoubleArray = coefficients[index]

	fun getConstants(): List<Double> = constants

	fun getConstant(index: Int): Double = constants[index]
}