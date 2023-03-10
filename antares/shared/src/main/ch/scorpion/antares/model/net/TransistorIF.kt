package ch.scorpion.antares.model.net

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

interface TransistorIF<T: Any> : Vertice {

	companion object {
		val DEFAULT_TRANSISTOR_TYPE = TransistorType.N

		const val SOURCE_PORT_ID = 1
		const val GATE_PORT_ID = 2
		const val DRAIN_PORT_ID = 3
	}

	val baseResourceKey: String

	val sourcePort: Port<T> get() = getPort(SOURCE_PORT_ID)
	val gatePort: Port<T> get() = getPort(GATE_PORT_ID)
	val drainPort: Port<T> get() = getPort(DRAIN_PORT_ID)

	var transistorType: TransistorType

	val isOn: Boolean

	override val type: String get() =
		when (transistorType) {
			TransistorType.N -> Translations.getString("$baseResourceKey.nType.name")
			TransistorType.P -> Translations.getString("$baseResourceKey.pType.name")
		}

	override val typeDesc: String? get() =
		when (transistorType) {
			TransistorType.N -> Translations.getOptionalString("$baseResourceKey.nType.desc")
			TransistorType.P -> Translations.getOptionalString("$baseResourceKey.pType.desc")
		}

	override fun write(writer: StoreWriter) {
		writer.writeString("type", transistorType.customName)
	}

	override fun read(reader: StoreReader) {
		if (reader.hasAttribute("type")) {
			transistorType = TransistorType.withName(reader.readString("type"))
		}
	}
}