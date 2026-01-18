package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.GraphEditorMockBuilder
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ConstantApplicationModeHolder
import ch.scorpion.jabbah.graph.ui.ScenarioViewMockBuilder
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImpl
import ch.scorpion.jabbah.graph.view.scenario.ScenarioStepImpl
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