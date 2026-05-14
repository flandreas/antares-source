package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
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
	var voltage: MagnitudeValue = MagnitudeValue(voltage, Magnitude.One, SIUnit.Volt)
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
		if (reader.hasAttribute("voltage")) {
			// Backward compatability before MagnitudeValue was introduced
			voltage = MagnitudeValue(reader.readDouble("voltage"), Magnitude.One, SIUnit.Volt)
		} else if (reader.hasAttribute("voltage${MagnitudeValue.MAGNITUDE_VALUE_EXT}")) {
			voltage = MagnitudeValue.read("voltage", reader, SIUnit.Volt)
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		voltage.write("voltage", writer)
	}

	/** ---- [AnalogElement] */

	override val voltageSourceCount: Int get() = 1

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		analysis.stampVoltageSource(analogElem.nodes[1], analogElem.nodes[0], analogElem.voltageSource, voltage.baseValue)
	}
}