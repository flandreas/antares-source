package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit.Error
import ch.scorpion.antares.model.signal.Bit.Undefined
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class TriStateBufferCalculator : VerticeCalculator<TriStateBufferGate> {

	override fun calculate(vertice: TriStateBufferGate, data: GraphActorData, signalHandler: SignalHandler) {
		val control = data.getSignal<DigitalSignal>(2)!!.bitAt(0)
		vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(
			when (control) {
				Undefined -> Word.undefined(vertice.bitWidth)
				Error -> Word.error(vertice.bitWidth)
				else -> if (vertice.enableLogic.evaluate(control.isSet)) {
					data.getSignal(1)!!
				} else {
					Word.undefined(vertice.bitWidth)
				}
			},
			signalHandler)
	}
}

open class TriStateBufferGate(
    bitWidth: BitWidth = BitWidth.BW_1,
    enableLogic: Logic = Logic.POSITIVE
) : CalculatingVertice(CALCULATOR) {

    companion object {
	    private const val BASE_RESOURCE_KEY = "library.element.TriStateBuffer"
	    private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
	    private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

	    const val ENABLE_PORT_NAME = "EN"
        val CALCULATOR = TriStateBufferCalculator()
    }

	val isOn: Boolean get() {
		val controlBit = getEnablePort().getIncomingSignal()?.bitAt(0)
		return controlBit?.let { enableLogic.evaluate(it.isSet) } ?: false
	}

    init {
        propagationDelay = 20

        addPort(DigitalPortImpl.createInput(Logic.POSITIVE, null, bitWidth))
        addPort(DigitalPortImpl.createInput(enableLogic, ENABLE_PORT_NAME, BitWidth.BW_1))
	    // Use InOut instead of Output to support visualization in FET Transistor view
	    addPort(DigitalPortImpl.createInOut(Logic.POSITIVE, null, bitWidth))
    }

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

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

    fun getInputPort(): DigitalPort = getInput<DigitalSignal>(1) as DigitalPort

    fun getEnablePort(): DigitalPort = getInput<DigitalSignal>(2) as DigitalPort

    fun getOutputPort(): DigitalPort = getOutput<DigitalSignal>() as DigitalPort
}
