package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class Resistor(
	resistance: Double = DEF_RESISTANCE
) : AbstractAnalogTwoPortVertice<Resistor>(CALCULATOR, "library.element.Resistor") {

	companion object {
		private const val DEF_RESISTANCE = 100.0

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Resistor> {
			override fun calculate(vertice: Resistor, data: GraphActorData, signalHandler: SignalHandler) {
				// TODO
			}
		}
	}

	var resistance: Double = resistance
		set(value) {
			if (field != value) {
				field = value
				stateChanged(reason = MAIN_PROPERTY_STATE)
			}
		}

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		resistance = reader.readDouble("resistance")
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("resistance", resistance)
	}
}