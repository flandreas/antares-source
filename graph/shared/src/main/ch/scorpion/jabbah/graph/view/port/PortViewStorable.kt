package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.io.*

/**
 * A "hull" class mainly used for cloning [PortView]s including their model [Port]s (if those are [Storable]).
 *
 * @param portView only `null` during deserialization
 */
class PortViewStorable<T: Any>(portView: PortView<T>? = null) : AbstractStorable() {

	var portView: PortView<T>? = portView

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
}