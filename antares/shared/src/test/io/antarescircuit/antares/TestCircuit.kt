package io.antarescircuit.antares

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.gate.NonUnaryLogicGate
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Or
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView

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