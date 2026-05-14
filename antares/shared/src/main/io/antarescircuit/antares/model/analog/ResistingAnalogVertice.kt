package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Ohm
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/** An [AnalogVertice] with an electrical resistance. */
interface ResistingAnalogVertice : AnalogVertice {

	val resistance: MagnitudeValue
}

/**
 * A base implementation of [ResistingAnalogVertice].
 */
abstract class AbstractResistingAnalogVertice<T: CalculatingVertice>(
	resistance: Double,
	calculator: VerticeCalculator<T>,
	baseResourceKey: String
) : AbstractAnalogTwoPortVertice<T>(calculator, baseResourceKey), ResistingAnalogVertice {

	override var resistance: MagnitudeValue = MagnitudeValue(resistance, Magnitude.One, Ohm)
		set(value) {
			if (field != value) {
				field = value
				stateChanged(reason = MAIN_PROPERTY_STATE)
			}
		}

	/** ---- [AnalogElement] */

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		try {
			analysis.stampResistor(analogElem.nodes[0], analogElem.nodes[1], resistance.baseValue)
		} catch (e: IllegalArgumentException) {
			throw IllegalStateException(e.message)
		}
	}

	/** ---- [Storable] */

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("resistance")) {
			// Backward compatability due to bug #740
			// Backward compatability before MagnitudeValue was introduced
			resistance = MagnitudeValue(reader.readDouble("resistance"), Magnitude.One, Ohm)
		} else if (reader.hasAttribute("resistance${MagnitudeValue.MAGNITUDE_VALUE_EXT}")) {
			resistance = MagnitudeValue.read("resistance", reader, Ohm)
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		resistance.write("resistance", writer)
	}
}