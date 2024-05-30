package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.signal.Bit.Error
import ch.scorpion.antares.model.signal.Bit.Undefined
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class TriStateBufferCalculator : VerticeCalculator<TriStateBufferGate> {

	override fun calculate(vertice: TriStateBufferGate, data: GraphActorData, signalHandler: SignalHandler) {
		val control = effectiveGateInputBit(data.getSignal<DigitalSignal>(2)!!.bitAt(0))
		val result = when (control) {
			Error -> DigitalSignalFactory.error(vertice.bitWidth)
			else -> {
				if (vertice.enableLogic.evaluate(control.isSet)) {
					calculateOutputValue(vertice, data.getSignal(1)!!)
				} else {
					DigitalSignalFactory.undefined(vertice.bitWidth)
				}
			}
		}
		vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(result, signalHandler)
	}

	private fun calculateOutputValue(vertice: TriStateBufferGate, inputValue: DigitalSignal): DigitalSignal {
		return DigitalSignalFactory.ofBits(inputValue.bits.map {
			when (it) {
				Undefined -> vertice.undefinedInputResult
				else -> it
			}
		})
	}
}

open class TriStateBufferGate(
    bitWidth: BitWidth = BitWidth.BW_1,
    enableLogic: Logic = Logic.POSITIVE
) : CalculatingVertice(CALCULATOR) {

    companion object {
	    private const val BASE_RESOURCE_KEY = "library.element.TriStateBuffer"
	    private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
	    private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

	    const val ENABLE_PORT_NAME = "EN"
        val CALCULATOR = TriStateBufferCalculator()
    }

	val isOn: Boolean get() {
		val controlBit = getEnablePort().getIncomingSignal()?.bitAt(0)
		return controlBit?.let { enableLogic.evaluate(it.isSet) } ?: false
	}

    init {
        propagationDelay = AbstractLogicGate.DEFAULT_PROPAGATION_DELAY

        addPort(DigitalPortImpl.createInput(Logic.POSITIVE, null, bitWidth))
        addPort(DigitalPortImpl.createInput(enableLogic, ENABLE_PORT_NAME, BitWidth.BW_1))
	    addPort(DigitalPortImpl.createTriStateOutput(Logic.POSITIVE, null, bitWidth))
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

	@Suppress("unused") // Reflection
	val isNegative: Boolean get() = enableLogic == Logic.NEGATIVE

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
	    bitWidth.write("bitWidth", writer)
        writer.writeString("logic", enableLogic.customName)
	    if (StringUtils.isNotEmpty(getInput<DigitalSignal>().name)) {
		    writer.writeString("inputName", getInput<DigitalSignal>().name!!)
	    }
	    if (StringUtils.isNotEmpty(getOutput<DigitalSignal>().name)) {
		    writer.writeString("outputName", getOutput<DigitalSignal>().name!!)
	    }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
	    bitWidth = BitWidth.read("bitWidth", reader)
        enableLogic = Logic.withName(reader.readString("logic"))
	    if (reader.hasAttribute("inputName")) {
		    getInput<DigitalSignal>().name = reader.readString("inputName")
	    }
	    if (reader.hasAttribute("outputName")) {
		    getOutput<DigitalSignal>().name = reader.readString("outputName")
	    }
    }

    /** ---- [Actor] */

    override fun executionStart(signalHandler: SignalHandler) {
        super.executionStart(signalHandler)
        requestActingAfter(signalHandler, propagationDelay.value / 2, createActorData(null))
    }

    /** ---- [TriStateBufferGate] */

    /** The result [Bit] if the input is [Bit.Undefined]. */
    open val undefinedInputResult: Bit get() = Bit.False

    fun getInputPort(): DigitalPort = getInput<DigitalSignal>(1) as DigitalPort

    fun getEnablePort(): DigitalPort = getInput<DigitalSignal>(2) as DigitalPort

    fun getOutputPort(): DigitalPort = getOutput<DigitalSignal>() as DigitalPort
}
