package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.analysis.CircuitAnalysisService
import ch.scorpion.antares.model.signal.Bit.False
import ch.scorpion.antares.model.signal.Bit.True
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.RealTimeServiceJvm
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CircuitAnalysisTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
			BaseModule.timeService = RealTimeServiceJvm()
		}
	}

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("dcdc4da3-90e7-46c7-a4b1-1b9518c44604"))
	}

	@Test
	fun shouldAnalyse() {
		val service = CircuitAnalysisService()
		val truthTable = service.analyse(openedCircuitView.graph as DigitalGraph)

		assertEquals(2, truthTable.inputColumnCount)
		assertEquals(1, truthTable.outputColumnCount)
		assertTrue(truthTable.hasInputName("A"))
		assertTrue(truthTable.hasInputName("B"))
		assertTrue(truthTable.hasOutputName("O"))

		assertEquals(listOf(False, True, True, False), truthTable.getColumnValues(2))
	}
}