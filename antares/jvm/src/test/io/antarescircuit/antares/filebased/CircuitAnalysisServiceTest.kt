package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.analysis.CircuitAnalysisService
import io.antarescircuit.antares.model.signal.Bit.False
import io.antarescircuit.antares.model.signal.Bit.True
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CircuitAnalysisServiceTest : AbstractFileBasedTest() {

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