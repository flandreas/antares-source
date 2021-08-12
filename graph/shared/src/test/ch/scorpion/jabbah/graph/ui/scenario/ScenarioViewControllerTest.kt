package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.TestEditorBuilder
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class ScenarioViewControllerTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val eventBus = EventBusImpl()
	private val graphView = GraphViewBuilder<Boolean>().build()
	private val editor = TestEditorBuilder().withDrawing(graphView).build()
	private val controller = ScenarioViewController(editor, GraphApplicationContextHolder(mockk(relaxed = true)), eventBus)

	init {
		ScenarioViewMockBuilder(controller)
		controller.graphView = graphView
	}

	@Test
	fun shouldSetCurrentScenarioOnGraphView() {
		val scenario = mockk<Scenario>(relaxed = true)
		eventBus.post(ScenarioSelectionEvent(graphView, scenario, null))
		assertSame(scenario, graphView.currentScenario)
		assertNull(graphView.currentScenarioStep)
	}

	@Test
	fun shouldSetCurrentScenarioStepInGraphView() {
		val scenario = mockk<Scenario>(relaxed = true)
		val scenarioStep = mockk<ScenarioStep>(relaxed = true)
		eventBus.post(ScenarioSelectionEvent(graphView, scenario, scenarioStep))
		assertSame(scenario, graphView.currentScenario)
		assertSame(scenarioStep, graphView.currentScenarioStep)
	}
}