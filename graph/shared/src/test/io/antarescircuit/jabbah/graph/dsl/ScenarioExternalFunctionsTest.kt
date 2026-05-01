package io.antarescircuit.jabbah.graph.dsl

import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty
import io.antarescircuit.jabbah.execution.issue.IssueCollector
import io.antarescircuit.jabbah.graph.AbstractGraphViewExecutionTest
import io.antarescircuit.jabbah.graph.model.StoringGraphActorData
import io.antarescircuit.jabbah.graph.model.TestVertice
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.Scenario
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioImpl
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScenarioExternalFunctionsTest : AbstractGraphViewExecutionTest() {

    private lateinit var issueCollector: IssueCollector
    private lateinit var builder: GraphViewBuilder<Boolean>
    private val drawingView = DrawingViewMockBuilder()

    private lateinit var testVerticeView: VerticeView<TestVertice>

    override fun getGraphView(): GraphView = builder.graphView

    override fun setup() {
        super.setup()
        issueCollector = IssueCollector()
        builder = GraphViewBuilder()
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
        val result = scenario.condition.invoke(scheduler, drawingView.build())

        assertTrue(result)
        assertEquals(0, issueCollector.size)
    }

    @Test
    fun shouldFailAllHaveStateIdleWithIllegalState() {
        val scenario = createScenario("haveAllState(42)")
        startSimulation()
        proceedUntilQueueIsEmpty()

        scenario.condition.invoke(scheduler, drawingView.build())

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

        val result = scenario.condition.invoke(scheduler, drawingView.build())

        assertTrue(result)
        assertEquals(0, issueCollector.size)
    }
}