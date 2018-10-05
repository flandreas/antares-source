package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.model.truthtable.TruthTableModel
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Performs a logical "NOR" function with the current input signals of a [Vertice].
 */
class NorCalculator<T : Vertice> : VerticeCalculator<T> {
    override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
        val outputPort = vertice.getOutput<DigitalSignal>()
        var hasUndefined = false

        for (port in vertice.getInputs()) {
            val signal = data.getSignal<DigitalSignal>(port.portId)!!
            if (signal.bitAt(0) == Bit.True) {
                outputPort.setOutgoingSignalBuffered(Word.of(Bit.False), signalHandler)
                return
            } else if (signal.bitAt(0) == Bit.Undefined) {
                hasUndefined = true
            }
        }

        if (hasUndefined) {
            outputPort.setOutgoingSignalBuffered(Word.of(Bit.Undefined), signalHandler)
        } else {
            outputPort.setOutgoingSignalBuffered(Word.of(Bit.True), signalHandler)
        }
    }
}

class NorGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate("library.element.NorGate", CALCULATOR, inputCount) {

    companion object {
        val CALCULATOR = NorCalculator<NorGate>()

        val TRUTH_TABLE = TruthTableModel(2, 1)
                .define(intArrayOf(0, 0), 1)
                .define(intArrayOf(0, 1), 0)
                .define(intArrayOf(1, 0), 0)
                .define(intArrayOf(1, 1), 0)
    }

    override fun createOutputPort(): OutputPort<DigitalSignal> {
        return DigitalPortImpl.createOutput(Logic.NEGATIVE)
    }
}

