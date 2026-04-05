package io.antarescircuit.antares.filebased.analog

import io.antarescircuit.antares.view.analog.AnalogCircuitInOutView
import io.antarescircuit.jabbah.base.UUID
import junit.framework.TestCase.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test

class CMOSInverterTest : AbstractAnalogFileBasedTest() {

	private lateinit var inputView: AnalogCircuitInOutView
	private lateinit var outputView: AnalogCircuitInOutView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("8da9290d-347b-476e-a680-efdb20c6ddb7"))

		inputView = openedCircuitView.getWithId(1) as AnalogCircuitInOutView
		outputView = openedCircuitView.getWithId(2) as AnalogCircuitInOutView
	}

	@Test
	fun shouldCalculateWithLowInput() {
		startSimulation()
		processUntilQueueIsEmpty()
		assertNoIssues()

		assertVoltage(5.0, outputView.model.signal!!.voltage)
		assertEquals("5.0 V", outputView.voltageLabel.text)
	}

	@Test
	fun shouldCalculateWithHighInput() {
		startSimulation()
		processUntilQueueIsEmpty()

		inputView.model.toggle(scheduler)
		processUntilQueueIsEmpty()

		assertNoIssues()
		assertVoltage(0.0, outputView.model.signal!!.voltage)
		assertEquals("0.0 V", outputView.voltageLabel.text)
	}
}