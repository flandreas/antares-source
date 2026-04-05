package io.antarescircuit.antares.model.output

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

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