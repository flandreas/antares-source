package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.model.truthtable.TruthTableModel
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Performs a NAND calculation by inverting the result of a [AndCalculator].
 */
class NandCalculator<T : Vertice> : VerticeCalculator<T> {
    override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
        vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(AndCalculator.calculate(vertice, data).not()), signalHandler)
    }
}

class NandGate(inputCount: InputCount = InputCount.TWO): AbstractDigitalGate("library.element.NandGate", CALCULATOR, inputCount) {

    companion object {
        val CALCULATOR = NandCalculator<NandGate>()

        val TRUTH_TABLE = TruthTableModel(2, 1)
                .define(intArrayOf(0, 0), 1)
                .define(intArrayOf(0, 1), 1)
                .define(intArrayOf(1, 0), 1)
                .define(intArrayOf(1, 1), 0)
    }

    override fun createOutputPort(): OutputPort<DigitalSignal> {
        return DigitalPortImpl.createOutput(Logic.NEGATIVE)
    }
}
