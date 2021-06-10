package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Represents a device that allows the user to press keys, which are translated to the corresponding
 * 7-bit ASCII code and stored in a buffer, from where they can be read when the clock input gets asserted.
 *
 * The entered keys are stored in an internal buffer. When the clock input is triggered while the read-enable
 * [InputPort] is set, the oldest key entry is removed from the buffer and forwarded to the [OutputPort].
 */
class Keyboard(
	bufferSize: Int = DEFAULT_BUFFER_SIZE
) : CalculatingVertice(KeyboardCalculator()) {

	companion object {
		private val LOG by logger(Keyboard::class)

		private const val BASE_RESOURCE_KEY = "library.element.Keyboard"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private const val DEFAULT_BUFFER_SIZE = 8

		private const val CLOCK_PORT_NAME = "CLK"
		private const val CLEAR_PORT_NAME = "CLR"
		private const val READ_ENABLE_PORT_NAME = "EN"
		private const val DATA_PORT_NAME = "D"
		private const val AVAILABLE_PORT_NAME = "AV"

		private val AVAILABLE_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.keyboard.availablePort.desc"))
		private val CLEAR_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.keyboard.clearPort.desc"))
		private val ENABLE_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.keyboard.enablePort.desc"))
		private val CLOCK_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.keyboard.clockPort.desc"))
		private val DATA_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.keyboard.dataPort.desc"))
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	private val buffer: MutableList<Byte> by lazy { mutableListOf<Byte>() }

	var bufferSize: Int = bufferSize
		set(value) {
			checkArgument(value >= 1, "bufferSize must not be smaller than 1")
			field = value
		}

	val bufferItemsCount: Int get() = buffer.size

	val isEmpty: Boolean get() = bufferItemsCount == 0

	val isFull: Boolean get() = bufferSize == bufferItemsCount

	val clockInput: DigitalPort get() = getPort<DigitalSignal>(CLOCK_PORT_NAME) as DigitalPort

	val clearInput: DigitalPort get() = getPort<DigitalSignal>(CLEAR_PORT_NAME) as DigitalPort

	val readEnableInput: DigitalPort get() = getPort<DigitalSignal>(READ_ENABLE_PORT_NAME) as DigitalPort

	val dataOutput: DigitalPort get() = getPort<DigitalSignal>(DATA_PORT_NAME) as DigitalPort

	val availableData: DigitalPort get() = getPort<DigitalSignal>(AVAILABLE_PORT_NAME) as DigitalPort

	private val isReadEnabled: Boolean get() = readEnableInput.getIncomingSignal() == Word.of(true)

	init {
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = CLOCK_PORT_NAME, trigger = Trigger.EDGE, description = CLOCK_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = CLEAR_PORT_NAME, description = CLEAR_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = READ_ENABLE_PORT_NAME, description = ENABLE_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.OUTPUT, name = DATA_PORT_NAME, bitWidth = BitWidth.BW_8, description = DATA_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.OUTPUT, name = AVAILABLE_PORT_NAME, description = AVAILABLE_PORT_DESC))

		propagationDelay = 1000
	}

	/** ---- [Actor] */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		buffer.clear()
		stateChanged(signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		buffer.clear()
		stateChanged(signalHandler)
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bufferSize", bufferSize)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bufferSize = reader.readInt("bufferSize")
	}

	/** ---- [Keyboard] */

	fun getBytes(): Iterator<Byte> = buffer.iterator()

	private fun clear(signalHandler: SignalHandler) {
		buffer.clear()
		dataOutput.setOutgoingSignalBuffered(Word.of(dataOutput.bitWidth, 0), signalHandler)
		updateState(signalHandler)
	}

	fun enter(byte: Byte, signalHandler: SignalHandler) {
		if (bufferItemsCount < bufferSize) {
			buffer.add(byte)
			stateChanged(signalHandler)
			requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, null))
		}
	}

	private fun updateState(signalHandler: SignalHandler) {
		availableData.setOutgoingSignalBuffered(Word.of(!isEmpty), signalHandler)
		if (isEmpty) {
			dataOutput.setOutgoingSignalBuffered(Word.allOf(dataOutput.bitWidth, Bit.False), signalHandler)
		} else {
			dataOutput.setOutgoingSignalBuffered(Word.of(dataOutput.bitWidth, buffer[0].toLong()), signalHandler)
		}
		stateChanged()
	}

	private fun consume(signalHandler: SignalHandler) {
		if (isReadEnabled && !isEmpty) {
			val data = buffer.removeAt(0)
			LOG.trace("consuming $data")
			updateState(signalHandler)
		}
	}

	private class KeyboardCalculator : VerticeCalculator<Keyboard> {

		companion object {
			private val LOG by logger(KeyboardCalculator::class)
		}

		override fun calculate(vertice: Keyboard, data: GraphActorData, signalHandler: SignalHandler) {
			when (data.changedPort) {
				null -> vertice.updateState(signalHandler)
				vertice.clockInput -> {
					if (vertice.clockInput.getIncomingSignal() == Word.of(true)) {
						vertice.consume(signalHandler)
					}
				}
				vertice.clearInput -> vertice.clear(signalHandler)
			}
		}
	}
}

