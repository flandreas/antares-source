package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.vertice.AdjustableBitWidth
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.vertice.AbstractInteractableVertice
import ch.scorpion.jabbah.graph.model.vertice.InteractableVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Represents a DIP (Dual in-line package) switch with a configurable [BitWidth].
 * The switch settings can only be changed during execution, and they are NOT made
 * persistent.
 * @param bitWidth the initial [BitWidth] of this [DipSwitch]
 */
class DipSwitch(
	bitWidth: BitWidth = BitWidth.BW_4
) : AbstractInteractableVertice<DigitalSignal>(CALCULATOR), AdjustableBitWidth {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.DipSwitch"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private const val DEF_RETAIN_VALUE = false

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<DipSwitch> {
			override fun calculate(vertice: DipSwitch, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.calculate(signalHandler)
				val output = vertice.getOutput<DigitalSignal>()
				output.setOutgoingSignalBuffered(vertice.signal, signalHandler)
			}
		}
	}

	init {
		addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, null, bitWidth))
		propagationDelay = LongValueImpl(1000)
		signal = DigitalSignalFactory.allOf(bitWidth, Bit.False)
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	/** The value to be set as current value when the simulation is started. */
	var initialValue: DigitalSignal = DigitalSignalFactory.allOf(bitWidth, Bit.False)
		set(value) {
			if (field != value) {
				field = value
				setSignal(value, null)
				stateChanged()
			}
		}

	/** If set to `true`, [signal] is retained between multiple execution runs.*/
	var retainValue: Boolean = DEF_RETAIN_VALUE

	var bitWidth: BitWidth
		get() = getDigitalPort().bitWidth
		set(value) {
			if (value != bitWidth) {
				getDigitalPort().bitWidth = value
				initialValue = initialValue.ofWidth(value)
				setSignal(DigitalSignalFactory.allOf(bitWidth, Bit.False), null)
			}
		}

	private var firstExecution: Boolean = true

	/** ---- [InteractableVertice] */

	override var interactivePropagationDelay: Long = Switch.DEF_PROP_DELAY.value

	/** ---- [AdjustableBitWidth] */

	override fun adjustBitWidth(port: DigitalPort, bitWidth: BitWidth): Boolean {
		this.bitWidth = bitWidth
		return true
	}

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [Actor] interface */

	val startupValue: DigitalSignal get() = if (firstExecution || !retainValue) {
		initialValue
	} else {
		DigitalSignalFactory.falseValue(bitWidth)
	}

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		if (firstExecution || !retainValue) {
			resetSignal(initialValue, signalHandler)
		}
	}

	override fun executionStart(signalHandler: SignalHandler) {
		requestActingAfter(signalHandler, propagationDelay.value, createActorData(null))
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		if (!retainValue) {
			resetSignal(initialValue, signalHandler)
		}
		setInteractionEnabled(true, signalHandler)
		stateChanged(signalHandler)
		firstExecution = false
	}

	/** ---- [Storable] */

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.read("bitWidth", reader)
		if (reader.hasAttribute("initialValue")) {
			initialValue = DigitalSignalFactory.of(bitWidth, reader.readLong("initialValue"))
		}
		if (reader.hasAttribute("retainValue")) {
			retainValue = reader.readBoolean("retainValue")
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		bitWidth.write("bitWidth", writer)
		if (initialValue != DigitalSignalFactory.allOf(bitWidth, Bit.False)) {
			writer.writeULong("initialValue", initialValue.getValue())
		}
		if (retainValue != DEF_RETAIN_VALUE) {
			writer.writeBoolean("retainValue", retainValue)
		}
	}

	/** ---- [DipSwitch] */

	fun setBit(index: Int, bit: Bit, signalHandler: SignalHandler) {
		if (enabled) {
			requestSetSignal(signal!!.withBit(index, bit), signalHandler)
		}
	}

	fun setValue(value: DigitalSignal, signalHandler: SignalHandler) {
		if (enabled) {
			requestSetSignal(value, signalHandler)
		}
	}

	private fun getDigitalPort(): DigitalPort {
		return getPort<DigitalPort>() as DigitalPort
	}
}