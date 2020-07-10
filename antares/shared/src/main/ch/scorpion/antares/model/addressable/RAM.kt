package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.edit.model.text.description.DescribableImpl
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * Represents a random access (i.e. writable) memory whose address width and data width can be specified.
 *
 * The content of a [RAM] is cleared when execution is started, which is why a [RAM] is not editable
 * in edit mode.
 */
class RAM(hasClock: Boolean = true) : CalculatingVertice(RAMCalculator()), Addressable {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.RAM"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private const val ADDRESS_PORT_NAME = "A"
		private const val CHIP_SELECT_PORT_NAME = "CS"
		private const val DATA_PORT_NAME = "D"
		private const val WRITE_PORT_NAME = "WR"
		private const val CLEAR_PORT_NAME = "CLR"
		private const val CLOCK_PORT_NAME = "CLK"

		private val ADDRESS_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.ram.addressPort.desc"))
		private val CHIP_SELECT_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.ram.chipSelectPort.desc"))
		private val DATA_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.ram.dataPort.desc"))
		private val WRITE_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.ram.writePort.desc"))
		private val CLEAR_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.ram.clearPort.desc"))
		private val CLOCK_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.ram.clockPort.desc"))
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	/** Determines whether this [RAM] has a clock input. */
	var hasClock: Boolean = false
		set(value) {
			if (value != field) {
				field = value
				if (value) {
					val clockPort = DigitalPortImpl(portType = PortType.INPUT, name = CLOCK_PORT_NAME, describable = CLOCK_PORT_DESC)
					clockPort.trigger = Trigger.EDGE
					addPort(clockPort)
				} else {
					removePort(getPort<DigitalSignal>(CLOCK_PORT_NAME))
				}
			}
		}

	/**
	 * Represents the last value of the address input, but gets only updated when the chip is selected (CS).
	 * Can be used for displaying the "current" (i.e. last) selected address.
	 */
	var currentSelectedAddress: Int = 0

	init {
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = ADDRESS_PORT_NAME, bitWidth = BitWidth.BW_8, describable = ADDRESS_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = CHIP_SELECT_PORT_NAME, describable = CHIP_SELECT_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = WRITE_PORT_NAME, describable = WRITE_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = CLEAR_PORT_NAME, describable = CLEAR_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INOUT, name = DATA_PORT_NAME, bitWidth = BitWidth.BW_8, describable = DATA_PORT_DESC))

		this.hasClock = hasClock
	}

	/** ---- [Addressable] interface */

	override val memory = Memory()

	override val isSelected: Boolean get() = getChipSelectInput().getIncomingSignal() == Word.of(true)

	override val currentAddress: Int get() = currentSelectedAddress

	override val maxAddress: Int get() = getAddressInput().bitWidth.power().toInt() - 1

	override val data: Long
		get() {
			val address = currentAddress
			if (address >= 0) {
				return memory.read(address)
			}
			return 0
		}

	override val addressWidth: BitWidth get() = getAddressInput().bitWidth

	override val dataWidth: BitWidth get() = getDataPort().bitWidth

	override val disassemblyWidth: Int get() = 0

	override fun clear() {
		memory.clear()
		update()
	}

	override fun update() {
		stateChanged()
	}

	override fun dataAt(address: Int): Long = memory.read(address)

	override fun setDataAt(address: Int, value: Long) {
		memory.write(address, value)
		update()
	}

	override fun disassemblyAt(address: Int): String = ""

	override fun commentAt(address: Int): String? = memory.readComment(address)

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("addressBitWidth", addressWidth.customName)
		writer.writeString("dataBitWidth", dataWidth.customName)
		writer.writeBoolean("clock", hasClock)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		setAddressWidth(BitWidth.withName(reader.readString("addressBitWidth")))
		setDataWidth(BitWidth.withName(reader.readString("dataBitWidth")))
		hasClock = reader.readBoolean("clock")
	}

	/** ---- [Actor] interface */

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		currentSelectedAddress = 0
		clear()
		getDataPort().setOutgoingSignal(Word.undefined(dataWidth), signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		clear()
	}

	/** ---- [RAM] */

	fun setAddressWidth(bitWidth: BitWidth) {
		getAddressInput().bitWidth = bitWidth
		stateChanged()
	}

	fun setDataWidth(bitWidth: BitWidth) {
		getDataPort().bitWidth = bitWidth
		stateChanged()
	}

	fun getAddressInput(): DigitalPort {
		val addressInput = getPort<DigitalSignal>(ADDRESS_PORT_NAME)
		return addressInput as DigitalPort
	}

	fun getChipSelectInput(): DigitalPort {
		val addressInput = getPort<DigitalSignal>(CHIP_SELECT_PORT_NAME)
		return addressInput as DigitalPort
	}

	fun getDataPort(): DigitalPort = getPort<DigitalSignal>(DATA_PORT_NAME) as DigitalPort

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

	fun read(address: Int): Long? = memory.read(address)

	fun write(address: Int, value: Long, signalHandler: SignalHandler? = null) {
		memory.write(address, value)
		stateChanged(signalHandler)
	}
}