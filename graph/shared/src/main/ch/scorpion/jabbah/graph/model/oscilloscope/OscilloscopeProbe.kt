package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef.Companion.CALCULATOR
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
) : CalculatingVertice(object : VerticeCalculator<OscilloscopeProbe<T>> {
        override fun calculate(vertice: OscilloscopeProbe<T>, data: GraphActorData, signalHandler: SignalHandler) {
            vertice.stateChanged(signalHandler)
        }
    }
) {
    init {
        addPort(portFactory.createPort<T>(PortType.INPUT))
    }
}