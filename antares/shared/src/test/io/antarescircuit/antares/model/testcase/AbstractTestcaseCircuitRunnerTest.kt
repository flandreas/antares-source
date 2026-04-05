package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.gate.TriStateBufferGateView
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.net.ProbeView
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.GraphView

abstract class AbstractTestcaseCircuitRunnerTest(private val probeOutput: Boolean) {

    protected lateinit var circuit: GraphView

    init {
        AntaresTestRule.configure()
    }

    protected fun buildAndGateCircuit() {
        val builder = TestCircuitBuilder("test")
        val a = builder.addInput("A")
        val b = builder.addInput("B")
        val out = if (probeOutput) {
            builder.addVerticeView(buildProbeView("O"))
        } else {
            builder.addOutput("O")
        }
        val andGate = builder.addVerticeView(LogicGateView.andGateView())
        builder.connect(a, andGate, andGate.model.getInput(1))
        builder.connect(b, andGate, andGate.model.getInput(2))
        builder.connect(andGate, out)
        circuit = builder.build()
    }

    protected fun buildTriStateBufferCircuit() {
        val builder = TestCircuitBuilder("test")
        val i = builder.addInput("I")
        val o = if (probeOutput) {
            builder.addVerticeView(buildProbeView("O"))
        } else {
            builder.addOutput("O")
        }
        val en = builder.addInput("EN")
        val gate = builder.addVerticeView(TriStateBufferGateView())
        builder.connect(i, gate, gate.model.getInput(1))
        builder.connect(en, gate, gate.model.getInput(2))
        builder.connect(gate, gate.model.getOutput(3), o)
        circuit = builder.build()
    }

    protected fun buildMultiBitNOPCircuit() {
        val builder = TestCircuitBuilder("test")
        val i = builder.add(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "I", bitWidth = BitWidth.BW_8, portType = PortType.INPUT)))
        val o = if (probeOutput) {
            builder.addVerticeView(buildProbeView("O", BitWidth.BW_8))
        } else {
            builder.add(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "O", bitWidth = BitWidth.BW_8, portType = PortType.OUTPUT)))
        }
        builder.connect(i, o)
        circuit = builder.build()
    }

    private fun buildProbeView(name: String, bitWidth: BitWidth = BitWidth.BW_1): ProbeView {
        val probeView = ProbeView()
        probeView.name = name
        probeView.bitWidth = bitWidth
        return probeView
    }
}