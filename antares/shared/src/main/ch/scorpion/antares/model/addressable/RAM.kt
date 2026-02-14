package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.vertice.AdjustableBitWidth
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.addressable.AddressableVertice.Companion.ADDRESS_PORT_NAME
import ch.scorpion.antares.model.addressable.AddressableVertice.Companion.CHIP_SELECT_PORT_NAME
import ch.scorpion.antares.model.addressable.AddressableVertice.Companion.DATA_PORT_NAME
import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.nonvolatile.NonVolatile
import ch.scorpion.jabbah.graph.model.nonvolatile.NonVolatileStorable
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.StringStorable


/**
 * Represents a random access (i.e. writable) memory whose address width and data width can be specified.
 *
 * The content of a [RAM] is cleared when execution is started, which is why a [RAM] is not editable
 * in edit mode.
 */
class RAM(
	hasClock: Boolean = true,
	separateDataPorts: Boolean = false
) : AbstractAddressable<RAM>(RAMCalculator()), NonVolatile {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.RAM"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private const val WRITE_PORT_NAME = "WR"
		private const val CLEAR_PORT_NAME = "CLR"
		private const val CLOCK_PORT_NAME = "CLK"
		private const val DATA_INPUT_PORT_NAME = "DI"

		private val ADDRESS_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.ram.addressPort.desc"))
		private val CHIP_SELECT_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.ram.chipSelectPort.desc"))
		private val DATA_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.ram.dataPort.desc"))
		private val DATA_INPUT_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.ram.dataInputPort.desc"))
		private val DATA_OUTPUT_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.ram.dataOutputPort.desc"))

		private val WRITE_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.ram.writePort.desc"))
		private val CLEAR_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.ram.clearPort.desc"))
		private val CLOCK_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.ram.clockPort.desc"))
	}

	override var type: String = TYPE
	override var typeDesc: String? = TYPE_DESC

	/** Determines whether this [RAM] has a clock input. */
	var hasClock: Boolean = false
		set(value) {
			if (value != field) {
				field = value
				if (value) {
					val clockPort = DigitalPortImpl(portType = PortType.INPUT, name = CLOCK_PORT_NAME, description = CLOCK_PORT_DESC)
					clockPort.trigger = Trigger.EDGE
					addPort(clockPort)
				} else {
					removePort(getPort<DigitalSignal>(CLOCK_PORT_NAME))
				}
			}
		}

	override var nonVolatile: Boolean = false

	/**
	 * Represents the last value of the address input, but gets only updated when the chip is selected (CS).
	 * Can be used for displaying the "current" (i.e. last) selected address.
	 */
	var currentSelectedAddress: Int = 0

	/**
	 * Allows application of [RAM] to disable the [AdjustableBitWidth] property. Used e.g. by the Video RAM,
	 * whose [BitWidth] for the data and address ports are determined by other properties.
	 */
	var isAdjustableBitWidth: Boolean = true

	/**
	 * If `true`, this [RAM] has two separate uni-directional ports for data input and output.
	 * If `false`, it has one bidirectional data input/output port.
	 */
	var separateDataPorts: Boolean = false
		set(value) {
			if (value != field) {
				field = value
				if (value) {
					val dataInputPort = DigitalPortImpl(portType = PortType.INPUT, bitWidth = dataWidth, name = DATA_INPUT_PORT_NAME, description = DATA_INPUT_PORT_DESC)
					addPort(dataInputPort)
					getPort<DigitalSignal>(DATA_PORT_NAME).portType = PortType.OUTPUT
					getPort<DigitalSignal>(DATA_PORT_NAME).description = Description(DATA_OUTPUT_PORT_DESC)
				} else {
					removePort(getPort<DigitalSignal>(DATA_INPUT_PORT_NAME))
					getPort<DigitalSignal>(DATA_PORT_NAME).portType = PortType.INOUT
					getPort<DigitalSignal>(DATA_PORT_NAME).description = Description(DATA_PORT_DESC)
				}
			}
		}

	val isWrite: Boolean get() = getWriteInput().getIncomingSignal() == DigitalSignalFactory.of(true)
	val isRead: Boolean get() = getWriteInput().getIncomingSignal() == DigitalSignalFactory.of(false)
	val isChipSelected: Boolean get() = getChipSelectInput().getIncomingSignal() != DigitalSignalFactory.of(true)
	val isChipNotSelected: Boolean get() = getChipSelectInput().getIncomingSignal() == DigitalSignalFactory.of(false)

	init {
		propagationDelay = AbstractLogicGate.DEFAULT_PROPAGATION_DELAY
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = ADDRESS_PORT_NAME, bitWidth = BitWidth.BW_8, description = ADDRESS_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = CHIP_SELECT_PORT_NAME, description = CHIP_SELECT_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = WRITE_PORT_NAME, description = WRITE_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = CLEAR_PORT_NAME, description = CLEAR_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INOUT, name = DATA_PORT_NAME, bitWidth = BitWidth.BW_8, description = DATA_PORT_DESC))

		this.hasClock = hasClock
		this.separateDataPorts = separateDataPorts
	}

	/** ---- [Addressable] interface */

	override val storesCells: Boolean get() = false

	override val isSelected: Boolean get() = getChipSelectInput().getIncomingSignal() == DigitalSignalFactory.of(true)

	override val currentAddress: Int get() = currentSelectedAddress

	override val disassemblyWidth: Int get() = 0

	override fun disassemblyAt(address: Int): String = ""

	override fun updateDataPortBitWidth(bitWidth: BitWidth) {
		super.updateDataPortBitWidth(bitWidth)
		if (separateDataPorts) {
			getDataInput()!!.bitWidth = bitWidth
		}
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("addressBitWidth", addressWidth.customName)
		writer.writeString("dataBitWidth", dataWidth.customName)
		writer.writeBoolean("clock", hasClock)
		if (nonVolatile) {
			writer.writeBoolean("nonVolatile", nonVolatile)
		}
		if (separateDataPorts) {
			writer.writeBoolean("separateDataPorts", separateDataPorts)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		addressWidth = BitWidth.withName(reader.readString("addressBitWidth"))
		dataWidth = BitWidth.withName(reader.readString("dataBitWidth"))
		hasClock = reader.readBoolean("clock")
		if (reader.hasAttribute("nonVolatile")) {
			nonVolatile = reader.readBoolean("nonVolatile")
		}
		if (reader.hasAttribute("separateDataPorts")) {
			separateDataPorts = reader.readBoolean("separateDataPorts")
		}
	}

	/** ---- [Actor] interface */

	override fun executionInitializeNonVolatile(signalHandler: SignalHandler, nonVolatileData: NonVolatileStorable?) {
		super<AbstractAddressable>.executionInitializeNonVolatile(signalHandler, nonVolatileData)
		currentSelectedAddress = 0
		clear()
		if (nonVolatile) {
			nonVolatileData?.let {
				val m = it.getContent("memory")
				if (m is StringStorable) {
					CompressedMemoryDump.read(memory, m.content)
				}
			}
		}
		getDataPort().setOutgoingSignalBuffered(DigitalSignalFactory.undefined(dataWidth), signalHandler)
	}

	override fun executionStoppedNonVolatile(signalHandler: SignalHandler, nonVolatileData: NonVolatileStorable?) {
		super<AbstractAddressable>.executionStoppedNonVolatile(signalHandler, nonVolatileData)
		if (nonVolatile) {
			nonVolatileData?.let {
				it.addChild(
					NonVolatileStorable(id).apply {
						setContent("memory", StringStorable(CompressedMemoryDump.write(memory, dataWidth)))
					}
				)
			}
		}
		clear()
	}

	/** ---- [CalculatingVertice] interface */

	override fun flush(signalHandler: SignalHandler, data: ActorData) {
		if (isRead || separateDataPorts) {
			super.flush(signalHandler, data)
		}
	}

	override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean =
		if (isAdjustableBitWidth) {
			super.adjustBitWidth(portId, bitWidth)
		} else {
			false
		}

	/** ---- [RAM] */

	fun getWriteInput(): DigitalPort = getPort<DigitalSignal>(WRITE_PORT_NAME) as DigitalPort

	fun getClearInput(): DigitalPort {
		val clearPort = getPort<DigitalSignal>(CLEAR_PORT_NAME)
		return clearPort as DigitalPort
	}

	fun getClockInput(): DigitalPort? {
		if (!hasClock) {
			return null
		}
		return getPort<DigitalSignal>(CLOCK_PORT_NAME) as DigitalPort
	}

	fun getDataInput(): DigitalPort? {
		if (!separateDataPorts) {
			return null
		}
		return getPort<DigitalSignal>(DATA_INPUT_PORT_NAME) as DigitalPort
	}

	fun getEffectiveDataInput(): DigitalPort {
		return if (separateDataPorts) {
			getPort<DigitalSignal>(DATA_INPUT_PORT_NAME) as DigitalPort
		} else {
			getPort<DigitalSignal>(DATA_PORT_NAME) as DigitalPort
		}
	}

	fun read(address: Int): ULong = memory.read(address)

	fun write(address: Int, value: ULong, signalHandler: SignalHandler? = null) {
		val oldValue = memory.read(address)
		memory.write(address, value)
		notifyDataChanged(address, oldValue, value)
		stateChanged(signalHandler)
	}
}