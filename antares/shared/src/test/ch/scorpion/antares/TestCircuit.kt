package ch.scorpion.antares

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.gate.OrGate
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.antares.view.gate.OrGateView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView

class TestCircuit {

    private val builder = TestCircuitBuilder("test")
    val andGateView: AndGateView
    val orGateView: OrGateView
    val wire: EdgeView<DigitalSignal>

    val circuitView: GraphView
        get() = builder.graphView

    init {
        andGateView = builder.addVerticeView(AndGateView())
        orGateView = builder.addVerticeView(OrGateView(orGate = OrGate(PortCount.THREE)))
        wire = builder.connect(andGateView, orGateView)
    }
}