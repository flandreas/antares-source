package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.io.*

/**
 * A "hull" class mainly used for cloning [VerticeView]s including their model [Vertice]s.
 *
 * @param verticeView only `null` during deserialization
 */
class VerticeViewStorable<T: Vertice>(verticeView: VerticeView<T>? = null) : Storable {

	var verticeView: VerticeView<T>? = verticeView
		private set

	/** ---- [Storable] interface */

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeStorable("view", verticeView!!)
		writer.writeStorable("model", verticeView!!.model)
	}

	override fun read(reader: StoreReader) {
		verticeView = reader.readStorable("view")
		reader.readStorable<Vertice>("model")
	}

	override fun getStorableChildren(): Iterator<Storable> {
		return listOf(verticeView!!.model).iterator()
	}
}