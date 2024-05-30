package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.jabbah.base.Translations
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

class LookupTable(
	addressBitWidth: BitWidth = BitWidth.BW_4,
	dataBitWidth: BitWidth = BitWidth.BW_1
) : AbstractAddressable<LookupTable>(CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.LookupTable"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val ADDRESS_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("library.element.LookupTable.addressPort.desc"))
		private val DATA_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("library.element.LookupTable.dataPort.desc"))

		private val CALCULATOR = Calculator()

		class Calculator : VerticeCalculator<LookupTable> {
			override fun calculate(vertice: LookupTable, data: GraphActorData, signalHandler: SignalHandler) {
				val address = vertice.getAddressInput().getIncomingSignal()
				val addressInt = address!!.toInt()
				if (addressInt == null) {
					vertice.getDataPort().setOutgoingSignalBuffered(DigitalSignalFactory.error(vertice.dataWidth), signalHandler)
				} else {
					vertice.getDataPort().setOutgoingSignalBuffered(DigitalSignalFactory.of(vertice.dataWidth, vertice.dataAt(addressInt)), signalHandler)
				}
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	init {
		propagationDelay = AbstractLogicGate.DEFAULT_PROPAGATION_DELAY
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = Addressable.ADDRESS_PORT_NAME, bitWidth = addressBitWidth, description = ADDRESS_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.OUTPUT, name = Addressable.DATA_PORT_NAME, bitWidth = dataBitWidth,
			signalRepresentation = DigitalSignalRepresentation.HEXADECIMAL, description = DATA_PORT_DESC, canBeUndefined = false))
	}

	/** ---- [Addressable] interface */

	override val storesCells: Boolean get() = true

	override val isSelected: Boolean get() = true

	override val maxAddress: Int get() = getAddressInput().bitWidth.maxValue.toInt()

	override val currentAddress: Int get() = getAddressInput().getIncomingSignal()?.toInt() ?: 0

	override val disassemblyWidth: Int get() = 0

	override fun disassemblyAt(address: Int): String = ""

	/** ---- [Actor] interface */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay.value / 2, createActorData(null))
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("addressBitWidth", addressWidth.customName)
		writer.writeString("dataBitWidth", dataWidth.customName)
		writer.writeString("content", CompressedMemoryDump.write(memory, dataWidth))
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		addressWidth = BitWidth.withName(reader.readString("addressBitWidth"))
		dataWidth = BitWidth.withName(reader.readString("dataBitWidth"))
		CompressedMemoryDump.read(memory, reader.readString("content"))
	}

	/** ---- [LookupTable] */

	fun fillFromTruthTable(truthTable: TruthTable, outputColumn: Int) {
		for (row in 0 until truthTable.rowsCount) {
			val value = truthTable.getValue(row, outputColumn)
			if (value.isDefined) {
				memory.write(row, value.numericalValue.toULong())
			} else {
				memory.write(row, 0UL)
			}
		}
	}
}