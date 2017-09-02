package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef.Companion.CALCULATOR
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * A [Vertice] that probes signals in a [Graph] in order to be tracked and displayed
 * on the view layer.
 */
class OscilloscopeProbe<T: Any> : CalculatingVertice(CALCULATOR) {

    val CALCULATOR = object : VerticeCalculator<OscilloscopeProbe<T>> {
        override fun calculate(vertice: OscilloscopeProbe<T>, data: GraphActorData, signalHandler: SignalHandler) {
            vertice.stateChanged(signalHandler)
        }

    }

    init {
        addPort(PortImpl<T>(PortType.INPUT))
    }
}