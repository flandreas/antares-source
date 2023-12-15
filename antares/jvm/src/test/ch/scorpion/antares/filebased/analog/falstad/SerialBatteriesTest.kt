package ch.scorpion.antares.filebased.analog.falstad

import ch.scorpion.antares.filebased.analog.kirchhoff.AbstractAnalogFileBasedTest
import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class SerialBatteriesTest : AbstractAnalogFileBasedTest() {

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("3566b7b9-5949-46d6-84e0-1853d6c4314a"))
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
		processUntilQueueIsEmpty()

		val net = analogGraphView.graph!!.withId(5) as AnalogNet
		assertVoltage(10.0, net.signal!!.voltage)
	}
}