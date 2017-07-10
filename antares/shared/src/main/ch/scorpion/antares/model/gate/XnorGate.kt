package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Performs a logical "XNOR" function with the current input signals of a [Vertice].
 */
class XnorCalculator<T : Vertice> : VerticeCalculator<T> {

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

        outputPort.setOutgoingSignalBuffered(Word.of(allFalse || allTrue), signalHandler)
    }
}

class XnorGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

    companion object {
        val CALCULATOR = XnorCalculator<XnorGate>()
    }

    override fun createOutputPort(): OutputPort<DigitalSignal> {
        return DigitalPortImpl.createOutput(Logic.NEGATIVE)
    }
}
