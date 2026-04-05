package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.io.*

/**
 * A "hull" class mainly used for cloning [VerticeView]s including their model [Vertice]s.
 *
 * @param verticeView only `null` during deserialization
 */
class VerticeViewStorable<T: Vertice>(verticeView: VerticeView<T>? = null) : AbstractStorable() {

	var verticeView: VerticeView<T>? = verticeView
		private set

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeStorable("view", verticeView!!)
		writer.writeStorable("model", verticeView!!.model)
	}

	override fun read(reader: StoreReader) {
		verticeView = reader.readStorable("view")
		reader.readStorable<Vertice>("model")
	}
}