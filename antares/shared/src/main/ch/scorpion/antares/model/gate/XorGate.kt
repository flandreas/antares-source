package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.model.truthtable.TruthTableModel
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Performs a logical "XOR" function with the current input signals of a [Vertice].
 */
class XorCalculator<T: Vertice> : VerticeCalculator<T> {
    override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
        val outputPort = vertice.getOutput<DigitalSignal>()
        var allTrue = true
        var allFalse = true

        for (port in vertice.getInputs()) {
            val signal = data.getSignal<DigitalSignal>(port.portId)!!
            allTrue = allTrue && signal.bitAt(0) == Bit.True
            allFalse = allFalse && signal.bitAt(0) == Bit.False
            if (signal.bitAt(0) == Bit.Undefined) {
                outputPort.setOutgoingSignalBuffered(Word.of(Bit.Undefined), signalHandler)
                return
            }
        }
        outputPort.setOutgoingSignalBuffered(Word.of(!allFalse && !allTrue), signalHandler)
    }
}

class XorGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

    companion object {
        val CALCULATOR = XorCalculator<XorGate>()

        val TRUTH_TABLE = TruthTableModel(2, 1)
                .define(intArrayOf(0, 0), 0)
                .define(intArrayOf(0, 1), 1)
                .define(intArrayOf(1, 0), 1)
                .define(intArrayOf(1, 1), 0)
    }
}
