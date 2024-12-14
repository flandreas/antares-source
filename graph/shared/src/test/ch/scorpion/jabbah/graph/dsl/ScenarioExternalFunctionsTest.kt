package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.execution.issue.IssueCollector
import ch.scorpion.jabbah.graph.AbstractGraphViewExecutionTest
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImpl
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScenarioExternalFunctionsTest : AbstractGraphViewExecutionTest() {

    private val issueCollector = IssueCollector()
    private val builder: GraphViewBuilder<Boolean> = GraphViewBuilder()
    private val drawingView = DrawingViewMockBuilder()

    override fun getGraphView(): GraphView = builder.graphView

    override fun setup() {
        super.setup()
        builder.addVerticeView(TestVerticeView())
        drawingView.withDrawing(builder.graphView)
        issueCollector.clear()
    }

    @Test
    fun shouldAllHaveStateIdle() {
        val result = callAllHaveState(0)

        assertEquals(0, issueCollector.size)
        assertTrue(result)
    }

    @Test
    fun shouldFailAllHaveStateIdleWithIllegalState() {
        callAllHaveState(42)

        assertEquals(1, issueCollector.size)
    }

    private fun callAllHaveState(state: Int): Boolean {
        val scenario = ScenarioImpl()
        scenario.graphView = builder.graphView
        scenario.conditionProperty = ScriptProperty("haveAllState($state)")
        getGraphView().scenarios.add(scenario)

        startSimulation()
        proceedUntilQueueIsEmpty()

        val result = scenario.condition.invoke(drawingView.build<Component>() as DrawingView<GraphView>)

        return result
    }
}