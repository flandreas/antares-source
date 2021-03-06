package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.TriStateBufferGate
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class Transistor(
	transistorType: TransistorType = DEFAULT_TRANSISTOR_TYPE
) : TriStateBufferGate(enableLogic = typeToLogic(transistorType)) {

	companion object {
		private val DEFAULT_TRANSISTOR_TYPE = TransistorType.N
		private const val BASE_RESOURCE_KEY = "library.element.Transistor"
		private val TYPE_N = Translations.getString("$BASE_RESOURCE_KEY.nType.name")
		private val TYPE_N_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.nType.desc")
		private val TYPE_P = Translations.getString("$BASE_RESOURCE_KEY.pType.name")
		private val TYPE_P_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.pType.desc")

		private fun typeToLogic(type: TransistorType): Logic =
			when(type) {
				TransistorType.P -> Logic.NEGATIVE
				TransistorType.N -> Logic.POSITIVE
			}
	}

	var transistorType: TransistorType = transistorType
		set(value) {
			if (field != value) {
				field = value
				enableLogic = typeToLogic(field)
				stateChanged()
			}
		}

	override val type: String get() =
		when (transistorType) {
			TransistorType.P -> TYPE_P
			TransistorType.N -> TYPE_N
		}

	override val typeDesc: String? get() =
		when (transistorType) {
			TransistorType.P -> TYPE_P_DESC
			TransistorType.N -> TYPE_N_DESC
		}

	fun getGatePort(): DigitalPort = getEnablePort()
	fun getSourcePort(): DigitalPort = getInputPort()
	fun getDrainPort(): DigitalPort = getOutputPort()

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (transistorType != DEFAULT_TRANSISTOR_TYPE) {
			writer.writeString("type", transistorType.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("type")) {
			transistorType = TransistorType.withName(reader.readString("type"))
		}
	}
}