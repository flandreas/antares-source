package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.app.CurrentApplicationVersion
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.io.*
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.project.Project

/**
 * A [Storable] bundle of [MetaGraph]s used for exporting / importing collections of [MetaGraph]s.
 *
 * Can be created (filled) by [MetaGraphRepository.createBundle]. Can only be loaded into a destination [Library]
 * if either [referencedSystemLibrary] is `null`, or the destination [Library] (if it is a [Project]) depends
 * on the referenced system [Library].
 */
class MetaGraphBundle(
	private val repository: MetaGraphRepository = LibraryModule.libraryHolder
) : AbstractStorable(), MetaGraphRepository {

	private val _metaGraphs = mutableListOf<MetaGraph>()

	/**
	 * The [UUIDS][UUID] of the system [Library] this [MetaGraphBundle] depends on while
	 * NOT containing [MetaGraph]'s of it. `null` if this [MetaGraphBundle] is fully self-contained.
	 * Must be set by the client of this class, if it is not deserialized from persistent store.
	 */
	val referencedSystemLibraryIds = mutableSetOf<UUID>()

	val metaGraphs: List<MetaGraph> get() = _metaGraphs

	fun add(metaGraph: MetaGraph): MetaGraphBundle {
		if (!_metaGraphs.contains(metaGraph)) {
			_metaGraphs.add(metaGraph)
		}
		return this
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		CurrentApplicationVersion.write(writer)
		if (referencedSystemLibraryIds.isNotEmpty()) {
			writer.writeUuids("systemLibs", referencedSystemLibraryIds)
		}
		writer.writeStorables("metaGraphs", metaGraphs.iterator())
	}

	override fun read(reader: StoreReader) {
		CurrentApplicationVersion.check(reader)

		_metaGraphs.clear()
		if (reader.hasAttribute("systemLib")) {
			// Backward compatibility
			referencedSystemLibraryIds.add(System.createUUID(reader.readString("systemLib")))
		}
		if (reader.hasAttribute("systemLibs")) {
			referencedSystemLibraryIds.addAll(reader.readUuids("systemLibs"))
		}

		try {
			LibraryModule.libraryHolder.wrapWith(this)
			// Make sure that resolutionDone() gets called
			reader.requestResolution(this, Reference("unwrap"))

			reader.readStorables<MetaGraph>("metaGraphs") {
				_metaGraphs.add(it)
			}
		} catch(e: Throwable) {
			LibraryModule.libraryHolder.unwrap()
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun resolutionDone() {
		super.resolutionDone()
		LibraryModule.libraryHolder.unwrap()
	}

	/** ---- [MetaGraphRepository] */

	private fun resolveLocalUuid(uuid: UUID): MetaGraph? =
		_metaGraphs.firstOrNull { it.containerDrawing.model.graphUUID == uuid }

	override fun getContainerLibraryElement(uuid: UUID): ContainerLibraryElement? = null

	override fun getMetaGraphUnwrapped(uuid: UUID): MetaGraph =
		getMetaGraph(uuid)

	override fun getMetaGraph(uuid: UUID): MetaGraph =
		resolveLocalUuid(uuid) ?: repository.getMetaGraphUnwrapped(uuid)

	override fun getOptionalMetaGraph(uuid: UUID): MetaGraph? =
		resolveLocalUuid(uuid)

	override fun containsMetaGraph(uuid: UUID): Boolean =
		resolveLocalUuid(uuid) != null

	override fun getContainingLibrary(uuid: UUID): Library? = null

	override fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID): Boolean = false

	override fun createBundle(metaGraph: MetaGraph): MetaGraphBundle {
		throw UnsupportedOperationException("not implemented")
	}

	override fun wrapWith(wrapper: MetaGraphRepository) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun unwrap() {
		throw UnsupportedOperationException("not implemented")
	}
}