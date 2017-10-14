package ch.scorpion.jabbah.graph.script

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [Graph] javascript DSL.
 */
class GraphDSLTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    private val gateway: ScriptGateway
    private val view = mock<DrawingView<GraphView<GraphElementView<*>>>>()
    private var graphView: TestGraphView
    private val signalHandler = mock<SignalHandler>()

    init {
        ScriptModule.resetScriptGateway()
        gateway = ScriptModule.scriptGateway
        TestTranslationsBuilder().withAnyKey()
        graphView = TestGraphView()
        whenever(view.drawing).thenReturn(graphView.graphView)
    }

    @Test
    fun test() {
        ScriptModule.scriptEngineProvider.invoke().eval(Script(code = "print('Hello World from Nashorn')", origin="Test"))
    }

    @Test
    fun shouldAccessGraphName() {
        val result = gateway.exec(Script(code ="return graph.name()", origin = "Test"), view) as String
        assertThat(result, `is`("AnyString"))
    }

    @Test
    fun shouldAccessGraphElement() {
        val result = gateway.exec(Script(code ="return graph.elem(1).id()", origin = "Test"), view) as Int
        assertThat(result, `is`(1))
    }

    @Test
    fun shouldAccessOutputValue() {
        graphView.v1.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
        val result = gateway.exec(Script(code ="return graph.elem(1).output()", origin = "Test"), view) as Boolean
        assertThat(result, `is`(true))
    }
}