package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A digital component that produces a periodically changing [DigitalSignal].
 */
class Clock : AbstractDigitalGate(CALCULATOR, InputCount.ZERO) {

    companion object {
        val CALCULATOR = object : VerticeCalculator<Clock> {
            override fun calculate(vertice: Clock, data: GraphActorData, signalHandler: SignalHandler) {
                vertice.setOn(signalHandler, !vertice.isOn)
            }
        }
    }

    var isOn: Boolean = false
        private set

    var isEnabled: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                stateChanged()
            }
        }

    init {
        propagationDelay = 1_000_000_000
    }

    /** ---- [AbstractDigitalGate] */

    override val minInputCount: InputCount get() = InputCount.ZERO

    override val maxInputCount: InputCount get() = InputCount.ZERO

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        if (!isEnabled) {
            writer.writeBoolean("enabled", false)
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("enabled")) {
            isEnabled = reader.readBoolean("enabled")
        }
    }

    /** ---- [Actor] interface */

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
        setOn(signalHandler, false)
    }

    /** ---- [Clock] */

    fun setOn(signalHandler: SignalHandler, on: Boolean) {
        this.isOn = on
        getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(this.isOn), signalHandler)
        stateChanged()
        if (isEnabled) {
            actorSupport.requestActingAfter(signalHandler, propagationDelay / 2, createActorData(null))
        }
    }
}