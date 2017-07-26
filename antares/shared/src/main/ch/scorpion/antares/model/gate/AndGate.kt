package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/** Performs a logical "AND" function with the current input signals of a [Vertice].*/
class AndCalculator<T : Vertice> : VerticeCalculator<T> {

    companion object {
        fun calculate(vertice: Vertice, data: GraphActorData, portFilter: (InputPort<*>) -> Boolean = { true }): Bit {
            var value = true
            var undefined = false

            for (port in vertice.getInputs().filter { portFilter(it) }) {
                val signal = data.getSignal<DigitalSignal>(port.portId)!!
                if (signal.bitAt(0) == Bit.Error) {
                    return Bit.Error
                }
                if (signal.bitAt(0) == Bit.Undefined) {
                    undefined = true
                } else {
                    value = value && signal.bitAt(0).isSet
                }
            }
            if (!value) {
                return Bit.False
            }
            return if (undefined) Bit.Error else Bit.True
        }
    }

    override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
        vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(calculate(vertice, data)), signalHandler)
    }
}

/** A digital gate that performs a logical AND operation. */
class AndGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {
    
    companion object {
        val CALCULATOR = AndCalculator<AndGate>()
    }

    fun calculate(portFilter: (InputPort<*>) -> Boolean): Bit {
        return AndCalculator.calculate(this, createActorData(null), portFilter)
    }
}
