package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.model.output.LightEmitterModel
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.element.AbstractGraphElement
import io.antarescircuit.jabbah.graph.model.vertice.EmptyVerticeCalculator
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Treating [LightBulb] as an element with constant resistance, i.e. independent of changing
 * temperature when the current increases.
 */
class LightBulb : AbstractResistingAnalogVertice<LightBulb>(
	DEF_RESISTANCE,
	EmptyVerticeCalculator,
	"library.element.LightBulb"
), LightEmitterModel {

	companion object {
		private const val DEF_RESISTANCE = 20.0
	}

	/** ---- [Storable] */

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("resistance")) {
			// Backward compatability due to bug #740
			resistance = reader.readDouble("resistance")
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("resistance", resistance)
	}

	/** ---- [AbstractGraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		stateChanged(null, LightEmitterModel.REASON_GRAPH_PARAM_CHANGED, graph)
	}

	/** ---- [AnalogVertice] */

	override fun handleAnalogPortChanged(port: AnalogPort, signalHandler: SignalHandler) {
		super.handleAnalogPortChanged(port, signalHandler)
		stateChanged(signalHandler)
	}

}