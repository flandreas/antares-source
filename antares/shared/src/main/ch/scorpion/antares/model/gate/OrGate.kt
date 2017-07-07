package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Performs a logical "OR" function with the current input signals of a [Vertice].
 */
class OrCalculator<T: Vertice> : VerticeCalculator<T> {
    override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
        val outputPort = vertice.getOutput<DigitalSignal>()
        var allUndefined = true

        for (port in vertice.getInputs()) {
            val signal = data.getSignal<DigitalSignal>(port.portId)!!
            allUndefined = allUndefined && signal.bitAt(0) == Bit.Undefined
            if (signal.bitAt(0) == Bit.True) {
                outputPort.setOutgoingSignalBuffered(Word.of(true), signalHandler)
                return
            }
        }
        if (allUndefined) {
            outputPort.setOutgoingSignalBuffered(Word.of(Bit.Undefined), signalHandler)
        } else {
            outputPort.setOutgoingSignalBuffered(Word.of(false), signalHandler)
        }
    }
}

class OrGate(inputCount: InputCount) : AbstractDigitalGate(CALCULATOR, inputCount) {

    constructor(): this(InputCount.TWO)

    companion object {
        val CALCULATOR = OrCalculator<OrGate>()
    }
}
