package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.jabbah.graph.model.OutputPort
import io.antarescircuit.jabbah.graph.model.vertice.EmptyVerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Produces [current] outgoing at [OutputPort] 1.
 */
class CurrentSource(
	current: Double = DEF_CURRENT
) : AbstractAnalogTwoPortVertice<CurrentSource>(EmptyVerticeCalculator, "library.element.CurrentSource") {

	companion object {
		private const val DEF_CURRENT = 0.1
	}

	var current: Double = current
		set(value) {
			if (field != value) {
				field = value
				stateChanged(reason = MAIN_PROPERTY_STATE)
			}
		}

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		current = reader.readDouble("current")
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("current", current)
	}

	/** ---- [AnalogElement] */

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		analysis.stampCurrentSource(analogElem.nodes[1], analogElem.nodes[0], current)
	}
}