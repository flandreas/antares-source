package ch.scorpion.antares.model.output

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable

class SevenSegmentDisplay(
	portScheme: SevenSegmentDisplayScheme = SevenSegmentDisplayScheme.COMBINED
) : CalculatingVertice("library.element.SevenSegmentDisplay", CALCULATOR) {

	companion object {

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<SevenSegmentDisplay> {
			override fun calculate(vertice: SevenSegmentDisplay, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.stateChanged()
			}
		}
	}

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