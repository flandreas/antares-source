package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.addressable.Addressable.Companion.ADDRESS_PORT_NAME
import ch.scorpion.antares.model.addressable.Addressable.Companion.CHIP_SELECT_PORT_NAME
import ch.scorpion.antares.model.addressable.Addressable.Companion.DATA_PORT_NAME
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.max


/**
 * A read-only memory whose address width and data width can be specified.
 */
class ROM : AbstractAddressable<ROM>(CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.ROM"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val ADDRESS_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.rom.addressPort.desc"))
		private val CHIP_SELECT_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.rom.chipSelectPort.desc"))
		private val DATA_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.rom.dataPort.desc"))

		val CALCULATOR = Calculator()

		class Calculator : VerticeCalculator<ROM> {
			override fun calculate(vertice: ROM, data: GraphActorData, signalHandler: SignalHandler) {
				if (vertice.isSelected) {
					val address = vertice.getAddressInput().getIncomingSignal()
					val addressInt = address!!.toInt()
					if (addressInt == null) {
						vertice.getDataPort().setOutgoingSignalBuffered(DigitalSignalFactory.error(vertice.dataWidth), signalHandler)
					} else {
						vertice.currentSelectedAddress = addressInt
						vertice.getDataPort().setOutgoingSignalBuffered(DigitalSignalFactory.of(vertice.dataWidth, vertice.dataAt(addressInt)), signalHandler)
					}
				} else {
					vertice.getDataPort().setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.dataWidth), signalHandler)
				}
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	/**
	 * A newline-separated list of [Disassembler] configurations consisting of operations in the form "regex=op".
	 */
	var disassemblerConfig: String = ""
		set(value) {
			field = value
			resetDisassembler()
			disassembleAll()
		}

	/**
	 * Represents the last value of the address input, but gets only updated when the chip is selected (CS).
	 * Can be used for displaying the "current" (i.e. last) selected address.
	 */
	var currentSelectedAddress: Int = 0

	/**
	 * If `true`, the data in [dataSource] is loaded every time the simulation is started.
	 * This eases editing data files with an external tool and eliminates the need to manually
	 * import data after every change.
	 */
	var loadDataSource: Boolean = false

	private val disassembler = Disassembler()

	/** Maps a memory cell address to its disassembly string.*/
	private val disassembly = mutableMapOf<Int, String>()

	/** Contains the number of characters of the longest disassembly entry.*/
	private var _disassemblyWidth: Int = 0

	init {
		propagationDelay = AbstractDigitalGate.DEFAULT_PROPAGATION_DELAY
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = ADDRESS_PORT_NAME, bitWidth = BitWidth.BW_8, description = ADDRESS_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = CHIP_SELECT_PORT_NAME, description = CHIP_SELECT_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.OUTPUT, name = DATA_PORT_NAME, bitWidth = BitWidth.BW_8,
			signalRepresentation = DigitalSignalRepresentation.HEXADECIMAL, description = DATA_PORT_DESC, canBeUndefined = true))
	}

	/** ---- [Addressable] interface */

	override val storesCells: Boolean get() = true

	override val isSelected: Boolean get() = getChipSelectInput().getIncomingSignal() == DigitalSignalFactory.of(true)

	override val currentAddress: Int get() = currentSelectedAddress

	override val maxAddress: Int get() = getAddressInput().bitWidth.maxValue.toInt()

	override val disassemblyWidth: Int get() = _disassemblyWidth

	override fun clear() {
		memory.clear()
		resetDisassembly()
		update()
		notifyDataChanged(null, null, null)
	}

	override fun update() {
		disassembleAll()
		super.update()
	}

	override fun disassemblyAt(address: Int): String = disassembly.getOrElse(address) { "" }

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("addressBitWidth", addressWidth.customName)
		writer.writeString("dataBitWidth", dataWidth.customName)
		writer.writeString("content", CompressedMemoryDump.write(memory, dataWidth))
		if (StringUtils.isNotEmpty(disassemblerConfig)) {
			writer.writeString("disassembler", disassemblerConfig)
		}
		writer.writeBoolean("loadDataSource", loadDataSource)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		addressWidth = BitWidth.withName(reader.readString("addressBitWidth"))
		dataWidth = BitWidth.withName(reader.readString("dataBitWidth"))
		CompressedMemoryDump.read(memory, reader.readString("content"))
		disassemblerConfig = reader.readOptionalString("disassembler") ?: ""
		if (reader.hasAttribute("loadDataSource")) {
			loadDataSource = reader.readBoolean("loadDataSource")
		}
	}

	/** --- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		if (loadDataSource && StringUtils.isNotBlank(dataSource)) {
			try {
				System.getFileContents(dataSource!!)?.let {
					MemoryDump.read(memory, it)
				}
			} catch (e: Exception) {
				BaseModule.eventBus.post(IssueImpl(
					IssueSeverity.Warning,
					Translations.getString("ROM.loadDataSource.loadError.txt"),
					null,
					"ROM (ID $id)",
					null)
				)
			}
		}
	}

	/** ---- [ROM]  */

	fun read(address: Int): ULong = memory.read(address)

	fun write(address: Int, value: ULong) {
		memory.write(address, value)
		disassembleCell(address)
	}

	private fun resetDisassembler() {
		disassembler.reset()
		resetDisassembly()
	}

	private fun resetDisassembly() {
		_disassemblyWidth = 0
		disassembly.clear()
	}

	private fun disassembleAll() {
		resetDisassembly()
		if (disassemblerConfig.isBlank()) {
			return
		}
		disassembler.operations(disassemblerConfig)
		memory.getNonZeroCells().forEach { disassembleCell(it.address) }
	}

	private fun disassembleCell(address: Int) {
		val value = BitOperation.longToHex(memory.read(address)).padStart(dataWidth.width / 4, '0')
		val d = disassembler.disassemble(value)
		disassembly[address] = d
		_disassemblyWidth = max(_disassemblyWidth, disassembly[address]!!.length)
	}
}