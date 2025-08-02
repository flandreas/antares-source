package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.DrawingViewMockBuilder
import ch.scorpion.jabbah.execution.issue.IssueCollector
import ch.scorpion.jabbah.graph.AbstractGraphViewExecutionTest
import ch.scorpion.jabbah.graph.model.StoringGraphActorData
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImpl
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScenarioExternalFunctionsTest : AbstractGraphViewExecutionTest() {

    private val issueCollector = IssueCollector()
    private val builder: GraphViewBuilder<Boolean> = GraphViewBuilder()
    private val drawingView = DrawingViewMockBuilder()

    private lateinit var testVerticeView: VerticeView<TestVertice>

    override fun getGraphView(): GraphView = builder.graphView

    override fun setup() {
        super.setup()
        testVerticeView = TestVerticeView()
        builder.addVerticeView(testVerticeView)
        drawingView.withDrawing(builder.graphView)
        issueCollector.clear()
    }

    @Test
    fun shouldAllHaveStateIdle() {
        val scenario = createScenario("haveAllState(0)")

        startSimulation()
        proceedUntilQueueIsEmpty()
        val result = scenario.condition.invoke(scheduler, drawingView.build<Component>() as DrawingView<GraphView>)

        assertTrue(result)
        assertEquals(0, issueCollector.size)
    }

    @Test
    fun shouldFailAllHaveStateIdleWithIllegalState() {
        val scenario = createScenario("haveAllState(42)")
        startSimulation()
        proceedUntilQueueIsEmpty()

        scenario.condition.invoke(scheduler, drawingView.build<Component>() as DrawingView<GraphView>)

        assertEquals(1, issueCollector.size)
    }

    private fun createScenario(condition: String): Scenario {
        val scenario = ScenarioImpl()
        scenario.graphView = builder.graphView
        scenario.conditionProperty = ScriptProperty(condition)
        getGraphView().scenarios.add(scenario)
        return scenario
    }

    @Test
    fun shouldYieldSimulationTime() {
        val scenario = createScenario("simulationTime() == 100")

        startSimulation()
        proceedUntilQueueIsEmpty()
        scheduler.requestActingAfter(testVerticeView.model, 100, StoringGraphActorData(null, null))
        proceedUntilQueueIsEmpty()

        val result = scenario.condition.invoke(scheduler, drawingView.build<Component>() as DrawingView<GraphView>)

        assertTrue(result)
        assertEquals(0, issueCollector.size)
    }
}