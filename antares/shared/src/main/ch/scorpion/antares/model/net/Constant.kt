package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [Vertice] that produces a configurable constant [DigitalSignal] at its single output.
 */
class Constant(
        value: Word = Word.of(Bit.False)
) : CalculatingVertice(CALCULATOR) {

    init {
        addPort(DigitalPortImpl.createOutput())
        propagationDelay = 1
    }

    companion object {
        val CALCULATOR = object : VerticeCalculator<Constant> {
            override fun calculate(vertice: Constant, data: GraphActorData, signalHandler: SignalHandler) {
                vertice.getOutput<DigitalSignal>().setOutgoingSignal(data.getSignal(1), signalHandler)
            }
        }
    }

    var value: Word = value
        set(value) {
            if (field != value) {
                field = value
                stateChanged()
            }
        }

    var bitWidth: BitWidth
        get() = (getOutput<DigitalSignal>() as DigitalPort).bitWidth
        set(newValue) {
            if (newValue != bitWidth) {
                (getOutput<DigitalSignal>() as DigitalPort).bitWidth = newValue
                value = Word.of(bitWidth, value.getValue())
                stateChanged()
            }
        }

    /** ---- [Actor] */

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
        actorSupport.requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(getOutput<DigitalSignal>(), value))
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeInt("bitWidth", bitWidth.width);
        writer.writeLong("value", value.getValue());
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        bitWidth = BitWidth.of(reader.readInt("bitWidth"))
        value = Word.of(bitWidth, reader.readLong("value"))
    }
}