package io.antarescircuit.antares.model.addressable

import io.antarescircuit.antares.model.addressable.AddressableVertice.Companion.ADDRESS_PORT_NAME
import io.antarescircuit.antares.model.addressable.AddressableVertice.Companion.DATA_PORT_NAME
import io.antarescircuit.antares.model.gate.AbstractLogicGate
import io.antarescircuit.antares.model.gate.CurrentUndefinedGateInputBehavior
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.Translation
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

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

		val CALCULATOR = Calculator()

		class Calculator : VerticeCalculator<LookupTable> {
			override fun calculate(vertice: LookupTable, data: GraphActorData, signalHandler: SignalHandler) {
				var address = vertice.getAddressInput().getIncomingSignal()
				if (address!!.isPartiallyUndefined) {
					address = CurrentUndefinedGateInputBehavior.value.definedValue(vertice.addressWidth)
				}
				val addressInt = address.toInt()
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
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = ADDRESS_PORT_NAME, bitWidth = addressBitWidth, description = ADDRESS_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.OUTPUT, name = DATA_PORT_NAME, bitWidth = dataBitWidth,
			signalRepresentation = DigitalSignalRepresentation.HEXADECIMAL, description = DATA_PORT_DESC, canBeUndefined = false))
	}

	/** ---- [Addressable] interface */

	override val storesCells: Boolean get() = true

	override val isSelected: Boolean get() = true

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