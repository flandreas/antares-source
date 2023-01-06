package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Port 1 is the plus pin, port 2 is the minus pin.
 */
class Battery(
	voltage: Double = DEF_VOLTAGE
) : CalculatingVertice(CALCULATOR){

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.Battery"
		private const val DEF_VOLTAGE = 5.0

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Battery> {
			override fun calculate(vertice: Battery, data: GraphActorData, signalHandler: SignalHandler) {
				// TODO
			}
		}
	}

	override val type: String get() = Translations.getString("${BASE_RESOURCE_KEY}.name")

	override val typeDesc: String? get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

	/** The constant voltage (in V) this [Battery] produces. */
	var voltage: Double = voltage
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	init {
		addPort(AnalogPort())
		addPort(AnalogPort())
		propagationDelay = 0
	}

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		voltage = reader.readDouble("voltage")
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("voltage", voltage)
	}
}