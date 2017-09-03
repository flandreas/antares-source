package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.graph.model.GraphElementListener

/**
 * A [Vertice] that collects signals from multiple [OscilloscopeProbe]s.
 * [Oscilloscope] has a variable amount of [InputPort]s. Changes of values at the [InputPort]
 * are not processed through the [SignalHandler], but directly communicated to registered [GraphElementListener]s.
 */
class Oscilloscope : AbstractVertice() {

    override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
        stateChanged(signalHandler)
    }
}