package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.base.logger

class TriStateBufferCalculator : VerticeCalculator<TriStateBufferGate> {

    override fun calculate(vertice: TriStateBufferGate, data: GraphActorData, signalHandler: SignalHandler) {
        val isEnabled = data.getSignal<DigitalSignal>(2)!!
        if (vertice.enableLogic.evaluate(isEnabled.bitAt(0).isSet)) {
            vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(data.getSignal(1), signalHandler)
        } else {
            vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.undefined(vertice.bitWidth), signalHandler)
        }
    }
}

class TriStateBufferGate(
    bitWidth: BitWidth = BitWidth.BW_1,
    enableLogic: Logic = Logic.POSITIVE
) : CalculatingVertice("library.element.TriStateBuffer", CALCULATOR) {

    companion object {
        val LOG by logger(TriStateBufferGate::class)
        const val ENABLE_PORT_NAME = "EN"
        val CALCULATOR = TriStateBufferCalculator()
    }

    init {
        propagationDelay = 20

        addPort(DigitalPortImpl.createInput(Logic.POSITIVE, null, bitWidth))
        addPort(DigitalPortImpl.createInput(enableLogic, ENABLE_PORT_NAME, BitWidth.BW_1))
        addPort(DigitalPortImpl.createTriStateOutput(Logic.POSITIVE, null, bitWidth))
    }

    var bitWidth: BitWidth = bitWidth
        set(value) {
            if (field != value) {
                field = value
                getInputPort().bitWidth = value
                getOutputPort().bitWidth = value
                stateChanged()
            }
        }

    var enableLogic: Logic = enableLogic
        set(value) {
            if (field != value) {
                field = value
                getEnablePort().logic = value
                stateChanged()
            }
        }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeInt("bitWidth", bitWidth.width)
        writer.writeString("logic", enableLogic.customName)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        bitWidth = BitWidth.of(reader.readInt("bitWidth"))
        enableLogic = Logic.withName(reader.readString("logic"))
    }

    /** ---- [Actor] */

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
        requestActingAfter(signalHandler, propagationDelay / 2, createActorData(null))
    }

    /** ---- [TriStateBufferGate] */

    fun getInputPort(): DigitalPort {
        return getInput<DigitalSignal>(1) as DigitalPort
    }

    fun getEnablePort(): DigitalPort {
        return getInput<DigitalSignal>(2) as DigitalPort
    }

    fun getOutputPort(): DigitalPort {
        return getOutput<DigitalSignal>() as DigitalPort
    }
}
