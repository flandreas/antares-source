package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import ch.scorpion.jabbah.graph.model.vertice.AbstractInteractableVertice
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
) : AbstractInteractableVertice(CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.DipSwitch"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<DipSwitch> {
			override fun calculate(vertice: DipSwitch, data: GraphActorData, signalHandler: SignalHandler) {
				val output = vertice.getOutput<DigitalSignal>()
				output.setOutgoingSignalBuffered(data.getSignal(1), signalHandler)
				vertice.enabled = true
			}
		}
	}

	init {
		addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, null, bitWidth))
		propagationDelay = 1000
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	/** The value to be set as current value when the simulation is started. */
	var initialValue: DigitalSignal = DigitalSignalFactory.allOf(bitWidth, Bit.False)
		set(value) {
			if (field != value) {
				field = value
				this.value = value
				stateChanged()
			}
		}

	/** The current value of this [DipSwitch]. */
	var value: DigitalSignal = DigitalSignalFactory.allOf(bitWidth, Bit.False)
		private set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	var bitWidth: BitWidth
		get() = getDigitalPort().bitWidth
		set(value) {
			if (value != bitWidth) {
				getDigitalPort().bitWidth = value
				initialValue = initialValue.ofWidth(value)
				this.value = DigitalSignalFactory.allOf(bitWidth, Bit.False)
				stateChanged()
			}
		}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		value = initialValue
		enabled = false
	}

	override fun executionStart(signalHandler: SignalHandler) {
		requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, value))
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		value = initialValue
		enabled = true
		stateChanged()
	}

	/** ---- [Storable] */

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.of(reader.readInt("bitWidth"))
		if (reader.hasAttribute("initialValue")) {
			initialValue = DigitalSignalFactory.of(bitWidth, reader.readLong("initialValue"))
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bitWidth", bitWidth.width)
		if (initialValue != DigitalSignalFactory.allOf(bitWidth, Bit.False)) {
			writer.writeULong("initialValue", initialValue.getValue())
		}
	}

	/** ---- [DipSwitch] */

	fun setBit(index: Int, bit: Bit, signalHandler: SignalHandler) {
		if (enabled) {
			value = value.withBit(index, bit)
			enabled = false
			requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, value))
		}
	}

	private fun getDigitalPort(): DigitalPort {
		return getPort<DigitalPort>() as DigitalPort
	}
}