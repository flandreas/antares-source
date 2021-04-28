package ch.scorpion.antares

import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class CascadedTransceiverTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("c49c71d6-ba22-49ee-89e5-f59102d0bdba"))
	}

	@Test
	fun shouldStart() {
		startSimulation()
		proceedUntilQueueIsEmpty()
	}
}