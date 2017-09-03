package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortFactory

/**
 * A [Vertice] that probes signals in a [Graph] in order to be tracked and displayed
 * on the view layer.
 *
 * TODO Refactoring: Split PortFactory into model and view parts to avoid dependency on GraphViewModule!
 */
class OscilloscopeProbe<T: Any>(
        portFactory: PortFactory = GraphViewModule.portFactory
) : AbstractVertice() {

    val history = SignalHistory<T>()

    init {
        addPort(portFactory.createPort<T>(PortType.INPUT))
    }

    /** ---- [Vertice] interface */

    override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
        stateChanged(signalHandler)
        if (input.getIncomingSignal() != null) {
            history.add(input.getIncomingSignal() as T, signalHandler.executionTime)
        }
    }

    /** ---- [Actor] interface */

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
        history.clear()
    }
}