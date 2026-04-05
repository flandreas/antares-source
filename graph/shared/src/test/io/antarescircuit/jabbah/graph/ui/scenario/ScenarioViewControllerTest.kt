package io.antarescircuit.jabbah.graph.ui.scenario

import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.GraphEditorMockBuilder
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ConstantApplicationModeHolder
import io.antarescircuit.jabbah.graph.ui.ScenarioViewMockBuilder
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioImpl
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioStepImpl
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class ScenarioViewControllerTest {

	private val eventBus = EventBusImpl()
	private val graphView: GraphView
	private val editor: Editor
	private val controller: ScenarioViewController

	init {
		GraphViewTestRule.configure()
		graphView = GraphViewBuilder<Boolean>().build()
		editor = GraphEditorMockBuilder().withDrawing(graphView).build()
		controller = ScenarioViewController(editor, mock(MockMode.autofill), GraphApplicationContextHolder(mock(MockMode.autofill)), ConstantApplicationModeHolder(ApplicationMode.EDIT), eventBus)

        ScenarioViewMockBuilder(controller)
		controller.graphView = graphView
	}

	@Test
	fun shouldSetCurrentScenarioOnGraphView() {
		val scenario = ScenarioImpl()
		eventBus.post(ScenarioSelectionEvent(graphView, scenario, null))
		assertSame(scenario, graphView.currentScenario)
		assertNull(graphView.currentScenarioStep)
	}

	@Test
	fun shouldSetCurrentScenarioStepInGraphView() {
		val scenario = ScenarioImpl()
		val scenarioStep = ScenarioStepImpl()
		eventBus.post(ScenarioSelectionEvent(graphView, scenario, scenarioStep))
		assertSame(scenario, graphView.currentScenario)
		assertSame(scenarioStep, graphView.currentScenarioStep)
	}
}