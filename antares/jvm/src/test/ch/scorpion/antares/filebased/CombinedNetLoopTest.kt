package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class CombinedNetLoopTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private lateinit var a: DigitalCircuitInOut

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("cf4af97f-9053-4805-a7d2-9b5f923019bf"))

		a = openedCircuitView.graph!!.withId(4) as DigitalCircuitInOut
	}

	@Test
	fun shouldAvoidCombinedNetLoop() {
		startSimulation()
	}
}