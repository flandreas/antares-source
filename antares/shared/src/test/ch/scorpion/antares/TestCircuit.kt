package ch.scorpion.antares

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.gate.NonUnaryLogicGate
import ch.scorpion.antares.model.gate.NonUnaryLogicGateType.Or
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView

class TestCircuit {

    private val builder = TestCircuitBuilder("test")
    val andGateView: LogicGateView
    val orGateView: LogicGateView
    val wire: EdgeView<DigitalSignal>

    val circuitView: GraphView
        get() = builder.graphView

    init {
        andGateView = builder.addVerticeView(LogicGateView.andGateView())
        orGateView = builder.addVerticeView(LogicGateView(gate = NonUnaryLogicGate(Or, PortCount.THREE)))
        wire = builder.connect(andGateView, orGateView)
    }
}