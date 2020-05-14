package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.io.*

/**
 * A "hull" class mainly used for cloning [PortView]s including their model [Port]s (if those are [Storable]).
 *
 * @param portView only `null` during deserialization
 */
class PortViewStorable<T: Any>(portView: PortView<T>? = null) : Storable {

	var portView: PortView<T>? = portView

	override var storableId: Int = 0

	/** ---- [Storable] interface */


	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeStorable("view", portView!!)
		if (portView!!.port is Storable) {
			writer.writeStorable("model", portView!!.port as Storable)
		}
	}

	override fun read(reader: StoreReader) {
		portView = reader.readStorable("view")
		if (reader.hasElement("model")) {
			reader.readStorable<Storable>("model")
		}
	}

	override fun getStorableChildren(): Iterator<Storable> {
		if (portView!!.port is Storable) {
			return listOf(portView!!.port as Storable).iterator()
		}
		return EmptyIterator()
	}
}