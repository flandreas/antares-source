package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.graph.model.OutputPort
import io.antarescircuit.jabbah.graph.model.vertice.EmptyVerticeCalculator
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A 2 [Port] source of electrical [current] outgoing at [OutputPort] 1.
 */
class CurrentSource(
	current: Double = DEF_CURRENT
) : AbstractAnalogTwoPortVertice<CurrentSource>(EmptyVerticeCalculator, "library.element.CurrentSource") {

	companion object {
		private const val DEF_CURRENT = 0.1
	}

	var current: MagnitudeValue = MagnitudeValue(current, Magnitude.One, SIUnit.Ampere)
		set (value) {
			if (field != value) {
				field = value
				stateChanged(reason = MAIN_PROPERTY_STATE)
			}
		}

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("current")) {
			// Backward compatability before MagnitudeValue was introduced
			current = MagnitudeValue(reader.readDouble("current"), Magnitude.One, SIUnit.Ampere)
		} else if (reader.hasAttribute("current${MagnitudeValue.MAGNITUDE_VALUE_EXT}")) {
			current = MagnitudeValue.read("current", reader, SIUnit.Ampere)
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		current.write("current", writer)
	}

	/** ---- [AnalogElement] */

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		analysis.stampCurrentSource(analogElem.nodes[1], analogElem.nodes[0], current.baseValue)
	}
}