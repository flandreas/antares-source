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

		private const val BASE_RESOURCE_KEY = "library.element.Transistor"
		val TYPE_N get() = Translations.getString("${BASE_RESOURCE_KEY}.nType.name")
		val TYPE_N_DESC get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.nType.desc")
		val TYPE_P get() = Translations.getString("${BASE_RESOURCE_KEY}.pType.name")
		val TYPE_P_DESC get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.pType.desc")

	}

	val sourcePort: Port<T> get() = getPort(SOURCE_PORT_ID)
	val gatePort: Port<T> get() = getPort(GATE_PORT_ID)
	val drainPort: Port<T> get() = getPort(DRAIN_PORT_ID)

	var transistorType: TransistorType

	val isOn: Boolean

	override val type: String get() =
		when (transistorType) {
			TransistorType.N -> TYPE_N
			TransistorType.P -> TYPE_P
		}

	override val typeDesc: String? get() =
		when (transistorType) {
			TransistorType.N -> TYPE_N_DESC
			TransistorType.P -> TYPE_P_DESC
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