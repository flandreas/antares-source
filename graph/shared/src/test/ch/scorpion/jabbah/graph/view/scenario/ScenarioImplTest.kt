package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.DrawingViewMockBuilder
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphInput
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphPortView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.*

class ScenarioImplTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		Translations.withAnyKey()
	}

	@Test
	fun shouldMoveStepToBegin() {
		val scenario = ScenarioImpl()
		val step1 = ScenarioStepImpl()
		val step2 = ScenarioStepImpl()
		val step3 = ScenarioStepImpl()
		scenario.addStep(step1)
		scenario.addStep(step2)
		scenario.addStep(step3)

		scenario.moveStep(step3, 0)

		assertEquals(0, scenario.indexOf(step3))
		assertEquals(1, scenario.indexOf(step1))
		assertEquals(2, scenario.indexOf(step2))
	}

	@Test
	fun shouldMoveStepToEnd() {
		val scenario = ScenarioImpl()
		val step1 = ScenarioStepImpl()
		val step2 = ScenarioStepImpl()
		val step3 = ScenarioStepImpl()
		scenario.addStep(step1)
		scenario.addStep(step2)
		scenario.addStep(step3)

		scenario.moveStep(step1, 3)

		assertEquals(0, scenario.indexOf(step2))
		assertEquals(1, scenario.indexOf(step3))
		assertEquals(2, scenario.indexOf(step1))
	}

	@Test
	fun shouldNotMoveUp() {
		val scenario = ScenarioImpl()
		val step1 = ScenarioStepImpl()
		val step2 = ScenarioStepImpl()
		val step3 = ScenarioStepImpl()
		scenario.addStep(step1)
		scenario.addStep(step2)
		scenario.addStep(step3)

		scenario.moveStep(step1, 1)

		assertEquals(0, scenario.indexOf(step1))
		assertEquals(1, scenario.indexOf(step2))
		assertEquals(2, scenario.indexOf(step3))
	}

	@Test
	fun shouldMoveStepUp() {
		val scenario = ScenarioImpl()
		val step1 = ScenarioStepImpl()
		val step2 = ScenarioStepImpl()
		val step3 = ScenarioStepImpl()
		scenario.addStep(step1)
		scenario.addStep(step2)
		scenario.addStep(step3)

		scenario.moveStep(step1, 2)

		assertEquals(0, scenario.indexOf(step2))
		assertEquals(1, scenario.indexOf(step1))
		assertEquals(2, scenario.indexOf(step3))
	}

	@Test
	fun shouldMoveStepDown() {
		val scenario = ScenarioImpl()
		val step1 = ScenarioStepImpl()
		val step2 = ScenarioStepImpl()
		val step3 = ScenarioStepImpl()
		scenario.addStep(step1)
		scenario.addStep(step2)
		scenario.addStep(step3)

		scenario.moveStep(step3, 1)

		assertEquals(0, scenario.indexOf(step1))
		assertEquals(1, scenario.indexOf(step3))
		assertEquals(2, scenario.indexOf(step2))
	}

	@Test
	fun shouldEvaluateConditionScriptToTrue() {
		val signalHandler = mock<SignalHandler>(MockMode.autofill)

		val graphView = GraphViewImpl()
		val drawingView = DrawingViewMockBuilder().withDrawing(graphView).build<Component>()
		val graphPortView = TestGraphPortView(model = GraphInputImpl(name = "I"))
		graphView.add(graphPortView)
		(graphPortView.model as GraphInput).setIncomingSignal(42L, signalHandler)

		val scenario = ScenarioImpl()
		scenario.graphView = graphView
		scenario.conditionProperty = ScriptProperty("I == 42")
		scenario.executionStart(graphView, signalHandler)

		val result = scenario.condition(signalHandler, drawingView as DrawingView<GraphView>)

		assertTrue(result)
	}

	@Test
	fun shouldEvaluateConditionScriptToFalse() {
		val signalHandler = mock<SignalHandler>(MockMode.autofill)

		val graphView = GraphViewImpl()
		val drawingView = DrawingViewMockBuilder().withDrawing(graphView).build<Component>()
		val graphPortView = TestGraphPortView(model = GraphInputImpl(name = "I"))
		graphView.add(graphPortView)
		(graphPortView.model as GraphInput).setIncomingSignal(99L, signalHandler)

		val scenario = ScenarioImpl()
		scenario.graphView = graphView
		scenario.conditionProperty = ScriptProperty("I == 42")

		val result = scenario.condition(signalHandler, drawingView as DrawingView<GraphView>)

		assertFalse(result)
	}

}