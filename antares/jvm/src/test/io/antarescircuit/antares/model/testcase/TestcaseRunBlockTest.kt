package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import junit.framework.TestCase.assertEquals
import kotlin.test.Test

/**
 * Tests the [Testcase] "Run" block feature using a T-FlipFlop from the standard library.
 */
class TestcaseRunBlockTest : AbstractStandardLibraryBasedCircuitTest() {

	private val testScript = """
			T   Q   '!Q'
		run {
			1   1   0
			0   1   0
			1   0   1
		}
		""".trimIndent()

	private lateinit var flipFlopView: SubGraphVerticeView<*>

	override fun createCircuit(): GraphView {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		flipFlopView = builder.add(
			LibraryModule.libraryHolder
			.getContainerLibraryElement(UUID("e0786c72-2fa2-4ec4-a1de-0a84b7281bec"))!!
			.getNewInstance()) as SubGraphVerticeView<*>

		val i = builder.addInput("T")
		val q = builder.addOutput("Q")
		val qNot = builder.addOutput("!Q")

		builder.connect(i, flipFlopView, flipFlopView.model.getInput("T"))
		builder.connect(flipFlopView, flipFlopView.model.getOutput("Q"), q)
		builder.connect(flipFlopView, flipFlopView.model.getOutput("!Q"), qNot)

		return builder.build()
	}

	@Test
	fun shouldRunTestBlockWithCircuit() {
		assertResult(TestcaseCircuitRunner("test", testScript, getCircuitView().graph as DigitalGraph).run())
	}

	@Test
	fun shouldRunTestBlockWithScript() {
		val script = flipFlopView.model.getGraph().script!!
		val execScriptAST = BaseModule.parserFactory(script, null).parse()
		assertResult(TestcaseScriptRunner("test", testScript, getCircuitView().graph as DigitalGraph, execScriptAST).run())
	}

	private fun assertResult(result: TestRunResult) {
		assertEquals(3, result.collector.size)
		for (vector in result.collector.testVectors) {
			assertEquals(Value.State.PASSED, vector.getValue(1).state)
			assertEquals(Value.State.PASSED, vector.getValue(2).state)
		}
	}
}