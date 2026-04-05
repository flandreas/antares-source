package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.jabbah.graph.model.vertice.EmptyVerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Port 1 is the plus pin, port 2 is the minus pin.
 */
class Battery(
	voltage: Double = DEF_VOLTAGE
) : AbstractAnalogTwoPortVertice<Battery>(
	EmptyVerticeCalculator,
	"library.element.Battery"
) {

	companion object {
		private const val DEF_VOLTAGE = 5.0
	}

	/** The constant voltage (in V) this [Battery] produces. */
	var voltage: Double = voltage
		set(value) {
			if (field != value) {
				field = value
				stateChanged(reason = MAIN_PROPERTY_STATE)
			}
		}

	val positivePort: AnalogPort get() = getPort<AnalogPort>(1) as AnalogPort
	val negativePort: AnalogPort get() = getPort<AnalogPort>(2) as AnalogPort

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		voltage = reader.readDouble("voltage")
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("voltage", voltage)
	}

	/** ---- [AnalogElement] */

	override val voltageSourceCount: Int get() = 1

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		analysis.stampVoltageSource(analogElem.nodes[1], analogElem.nodes[0], analogElem.voltageSource, voltage)
	}
}