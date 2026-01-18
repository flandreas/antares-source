package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandlerMockBuilder
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistories
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import kotlin.test.Test
import kotlin.test.assertEquals

class OscilloscopeViewTest {

    private val signalHandler = SignalHandlerMockBuilder()
    private val oscilloscopeView: OscilloscopeView
    private val graphView: GraphView

    init {
        GraphViewTestRule.configure()
        oscilloscopeView = OscilloscopeView()
        graphView = GraphViewImpl().also { it.add(oscilloscopeView) }
    }

    @Test
    fun shouldUpdateMaxTime() {
        val probeView = createRow()
        oscilloscopeView.model.executionInitialize(signalHandler.build())
        oscilloscopeView.model.executionStart(signalHandler.build())
        input(probeView, 100, true)
        input(probeView, 200, false)
        assertEquals(200L, oscilloscopeView.model.maxTime)
    }

    @Test
    fun shouldTruncateEntries() {
        BaseModule.properties.customize(SignalHistories.PROP_BUFFER_SIZE, 2)
        val probeView = createRow()
        oscilloscopeView.model.executionInitialize(signalHandler.build())
        oscilloscopeView.model.executionStart(signalHandler.build())
        input(probeView, 100, true)
        input(probeView, 200, false)
        input(probeView, 300, true)
    }

    private fun createRow(): OscilloscopeProbeVerticeView<Boolean> {
        val name = oscilloscopeView.addRow()
        val probeView = OscilloscopeProbeVerticeView<Boolean>(name)
        graphView.add(probeView)
        return probeView
    }

    private fun input(probeView: OscilloscopeProbeVerticeView<Boolean>, time: Long, signal: Boolean) {
        signalHandler.withExecutionTime(time)
        probeView.model.getInput<Boolean>().setIncomingSignal(signal, signalHandler.build())
    }
}