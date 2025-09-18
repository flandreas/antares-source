package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.Logic
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

abstract class AbstractSegmentDisplay<T : Vertice>(
	calculator: VerticeCalculator<T>
) : CalculatingVertice(calculator), LightEmitterModel {

	var logic: Logic = Logic.POSITIVE

	abstract fun inputValueOf(bitName: String): Boolean

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		stateChanged(null, LightEmitterModel.REASON_GRAPH_PARAM_CHANGED, graph)
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (logic != Logic.POSITIVE) {
			writer.writeString("logic", logic.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("logic")) {
			logic = Logic.withName(reader.readString("logic"))
		}
	}
}