package ch.scorpion.jabbah.base.math

import ch.scorpion.jabbah.base.module.BaseModule
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.LUDecomposition
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Tests integration of Apache Commons Math linear algebra.*/
class SolveLinearSystemTest {

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

	/** Example system from https://ultimateelectronicsbook.com/solving-circuit-systems/ ("A simple example"). */
	@Test
	fun shouldSolveLinearSystem() {
		val coefficients = Array2DRowRealMatrix(arrayOf(
			arrayOf(1.0, 0.0, 0.0).toDoubleArray(),
			arrayOf(1.0, -1.0, -100.0).toDoubleArray(),
			arrayOf(0.0, 1.0, -100.0).toDoubleArray(),
		))
		val constants = ArrayRealVector(arrayOf(12.0, 0.0, 0.0).toDoubleArray(), false)
		val solution = LUDecomposition(coefficients).solver.solve(constants)

		assertEquals(12.0, solution.getEntry(0))
		assertEquals(6.0, solution.getEntry(1))
		assertEquals(0.06, solution.getEntry(2))
	}
}