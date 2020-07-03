package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [Graph] javascript DSL.
 */
class GraphDSLTest {

	companion object {
		init {
			Translations.withAnyKey()
			GraphViewTestRule.configure()
		}
	}

	private val gateway: ScriptGateway
	private val view = mockk<DrawingView<GraphView>>(relaxed = true)
	private var graphView: TestGraphView
	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	init {
		ScriptModule.resetScriptGateway()
		gateway = ScriptModule.scriptGateway
		Translations.language = Language.German
		Translations.withAnyKey()
		graphView = TestGraphView()
		every { view.drawing } returns graphView.graphView
	}

	@Test
	fun test() {
		ScriptEngine().eval(Script(code = "print('Hello World from Nashorn')", origin = "Test"))
	}

	@Test
	fun shouldAccessGraphName() {
		val result = gateway.exec(Script(code = "return graph.name()", origin = "Test"), view) as String
		assertEquals("Ohne Namen", result)
	}

	@Test
	fun shouldAccessGraphElement() {
		val result = gateway.exec(Script(code = "return graph.elem(1).id()", origin = "Test"), view) as Int
		assertEquals(1, result)
	}

	@Test
	fun shouldAccessOutputValue() {
		graphView.v1.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
		val result = gateway.exec(Script(code = "return graph.elem(1).output()", origin = "Test"), view) as Boolean
		assertTrue(result)
	}
}