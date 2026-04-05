package io.antarescircuit.antares.model.output

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

class SevenSegmentDisplay(
	portScheme: SevenSegmentDisplayScheme = SevenSegmentDisplayScheme.COMBINED
) : AbstractSegmentDisplay<SevenSegmentDisplay>(CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.SevenSegmentDisplay"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<SevenSegmentDisplay> {
			override fun calculate(vertice: SevenSegmentDisplay, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.stateChanged(signalHandler)
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	var portScheme: SevenSegmentDisplayScheme = portScheme
		set(value) {
			if (value != field) {
				field = value
				clearPorts()
				field.createPorts(this)
				stateChanged()
			}
		}

	init {
		portScheme.createPorts(this)
	}

	override fun inputValueOf(bitName: String): Boolean = portScheme.inputValueOf(this, bitName)

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("portScheme", portScheme.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		portScheme = SevenSegmentDisplayScheme.withName(reader.readString("portScheme"))
	}
}