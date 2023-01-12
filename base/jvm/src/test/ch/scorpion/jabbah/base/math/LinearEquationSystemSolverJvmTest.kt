package ch.scorpion.jabbah.base.math

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LinearEquationSystemSolverJvmTest {

	@BeforeTest
	fun setup() {
		BaseModuleJvm.require()
	}

	@Test
	fun shouldSolve() {
		val coefficients = arrayOf(
			arrayOf(1.0, 0.0, 0.0).toDoubleArray(),
			arrayOf(1.0, -1.0, -100.0).toDoubleArray(),
			arrayOf(0.0, 1.0, -100.0).toDoubleArray())

		val constants = arrayOf(12.0, 0.0, 0.0).toDoubleArray()

		val solution = BaseModule.linearEquationSystemSolver.solve(coefficients, constants)

		assertEquals(12.0, solution[0])
		assertEquals(6.0, solution[1])
		assertEquals(0.06, solution[2])
	}
}