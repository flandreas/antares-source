package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Delays a signal change for a given time.
 */
class DelayGateCalculator : VerticeCalculator<DelayGate> {
    override fun calculate(vertice: DelayGate, data: GraphActorData, signalHandler: SignalHandler) {
        vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(data.getSignal(1), signalHandler)
    }
}

class DelayGate : AbstractDigitalGate(CALCULATOR, InputCount.ONE) {

    companion object {
        val CALCULATOR = DelayGateCalculator()
    }

    init {
        propagationDelay = 20
    }

    /** The delay in nanoseconds.*/
    var delay: Long
        get() = propagationDelay
        set(value) {
            propagationDelay = value
        }

    override val minInputCount: InputCount get() = InputCount.ONE
    override val maxInputCount: InputCount get() = InputCount.ONE

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeLong("delay", delay)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        delay = reader.readLong("delay")
    }
}
