package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.image.ImageData
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.edit.model.image.ImageRepository
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.MetaGraphBundle
import io.antarescircuit.jabbah.graph.MetaGraphRepository

/**
 * Holds the one and only [Library].
 */
interface LibraryHolder : MetaGraphRepository, ImageRepository {
	var l: Library?
	val library: Library
}

class LibraryHolderImpl(
	l: Library? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) : LibraryHolder {

	companion object {
		private val LOG by logger(LibraryHolder::class)
	}

    override var l: Library? = l
        set(value) {
	        if (field != value) {
		        LOG.trace("LibraryHolder: setting current Library to '${value?.name}'")
		        field?.dispose()
		        field = value
		        eventBus.post(CurrentLibraryEvent(field))
	        }
        }

    override val library: Library get() = l!!

	/** ---- [MetaGraphRepository] */

	override fun getContainerLibraryElement(uuid: UUID): ContainerLibraryElement? =
		library.getContainerLibraryElement(uuid)

	override fun getMetaGraph(uuid: UUID): MetaGraph = library.getMetaGraph(uuid)

	override fun getMetaGraphUnwrapped(uuid: UUID): MetaGraph = library.getMetaGraphUnwrapped(uuid)

	override fun getOptionalMetaGraph(uuid: UUID): MetaGraph? = l?.getOptionalMetaGraph(uuid)

	override fun containsMetaGraph(uuid: UUID): Boolean = library.containsMetaGraph(uuid)

	override fun getContainingLibrary(uuid: UUID): Library? = library.getContainingLibrary(uuid)

	override fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID): Boolean =
		library.graphContainsRecursively(graphUUID, graphElementUUID)

	override fun createBundle(metaGraph: MetaGraph): MetaGraphBundle = library.createBundle(metaGraph)

	override fun wrapWith(wrapper: MetaGraphRepository) {
		library.wrapWith(wrapper)
	}

	override fun unwrap() {
		library.unwrap()
	}

	/** ---- [ImageRepository] */

	override fun getImage(uuid: UUID): ImageData? = library.getImage(uuid)

	override fun getAllImageIds(): List<ImageIdentification> = library.getAllImageIds()
}

/** Posted on [EventBus] when the current [Library] has changed.*/
data class CurrentLibraryEvent(val library: Library?)