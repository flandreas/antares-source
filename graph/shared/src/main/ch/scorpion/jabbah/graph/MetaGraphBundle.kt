package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.project.Project

/**
 * A [Storable] bundle of [MetaGraph]s used for exporting / importing collections of [MetaGraph]s.
 *
 * Can be created (filled) by [MetaGraphRepository.createBundle]. Can only be loaded into a destination [Library]
 * if either [referencedSystemLibrary] is `null`, or the destination [Library] (if it is a [Project]) depends
 * on the referenced system [Library].
 */
class MetaGraphBundle : Storable {

	private val _metaGraphs = mutableListOf<MetaGraph>()

	/**
	 * The [UUID] of the system [Library] this [MetaGraphBundle] depends on while NOT containing [MetaGraph]'s of it.
	 * `null` if this [MetaGraphBundle] is fully self-contained. Must be set by the client of this class, if it is
	 * not deserialized from persistent store.
	 */
	var referencedSystemLibrary: UUID? = null

	val metaGraphs: List<MetaGraph> get() = _metaGraphs

	fun add(metaGraph: MetaGraph): MetaGraphBundle {
		if (!_metaGraphs.contains(metaGraph)) {
			_metaGraphs.add(metaGraph)
		}
		return this
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		referencedSystemLibrary?.let {
			writer.writeString("systemLib", it.toString())
		}
		writer.writeStorables("metaGraphs", metaGraphs.iterator())
	}

	override fun read(reader: StoreReader) {
		_metaGraphs.clear()
		if (reader.hasAttribute("systemLib")) {
			referencedSystemLibrary = System.createUUID(reader.readString("uuid"))
		}
		for (metaGraph in reader.readStorables<MetaGraph>("metaGraphs")) {
			reader.requestResolution(this, Reference(
				name = "metaGraph",
				additionalInfo = metaGraph,
				resolveAfter = listOf(reader.getGlobalId(metaGraph))
			))
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		if ("metaGraph" == reference.name) {
			_metaGraphs.add(reference.additionalInfo as MetaGraph)
		}
	}
}