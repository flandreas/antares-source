package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.output.LightEmitterModel
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.element.AbstractGraphElement
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

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