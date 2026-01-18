package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.filebased.AbstractFileBasedTest
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.jabbah.base.math.near
import kotlin.test.assertTrue

abstract class AbstractAnalogFileBasedTest : AbstractFileBasedTest() {

	protected val analogGraphView: AnalogGraphView get() = openedCircuitView as AnalogGraphView

	protected fun assertCurrent(expected: Double, actual: Double) {
		assertTrue(actual.near(expected, 0.01), "Expected current $expected, but was $actual")
	}

	protected fun assertVoltage(expected: Double, actual: Double) {
		assertTrue(actual.near(expected, 0.01), "Expected voltage $expected, but was $actual")
	}
}