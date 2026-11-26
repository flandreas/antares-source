package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.library.LibraryModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FlipFlopScriptTestcaseTest {

	@BeforeTest
	fun setup() {
		AntaresTestRule.configure()
		AbstractStandardLibraryBasedCircuitTest.setupLibrary()
	}

	@Test
	fun shouldMatchNegativeTFlipFlopLibraryTestcase() {
		val metaGraph = LibraryModule.libraryHolder.getMetaGraph(UUID("ce3245e3-3b2c-405f-9d1b-9650823acfe7"))
		val graph = metaGraph.graph.model as DigitalGraph
		val result = TestcaseService.run(metaGraph, graph.testcases.testcases).single()

		assertNull(result.error)
		val scriptResult = result.scriptResults!!
		assertNull(scriptResult.errorMessage)
		assertEquals(5, scriptResult.collector.size)
		assertEquals(0, scriptResult.failedCount)
		assertEquals(0UL, scriptResult.collector.get(0).getValue(1).value.toLong())
	}

	@Test
	fun shouldMatchPresetClearDFlipFlopLibraryTestcase() {
		val metaGraph = LibraryModule.libraryHolder.getMetaGraph(UUID("f5ffee61-6d3e-453e-a352-e702a06b11d2"))
		val graph = metaGraph.graph.model as DigitalGraph
		val result = TestcaseService.run(metaGraph, graph.testcases.testcases).single()

		assertNull(result.error)
		val scriptResult = result.scriptResults!!
		assertNull(scriptResult.errorMessage)
		assertEquals(7, scriptResult.collector.size)
		assertEquals(0, scriptResult.failedCount)
		assertEquals(0UL, scriptResult.collector.get(0).getValue(4).value.toLong())
	}
}
