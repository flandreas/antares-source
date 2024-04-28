package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.image.ImageData
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.edit.model.image.ImageRepository
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphBundle
import ch.scorpion.jabbah.graph.MetaGraphRepository

/**
 * Holds the one and only [Library].
 */
class LibraryHolder(
	l: Library? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) : MetaGraphRepository, ImageRepository {

	companion object {
		private val LOG by logger(LibraryHolder::class)
	}

    var l: Library? = l
        set(value) {
	        if (field != value) {
		        LOG.trace("LibraryHolder: setting current Library to '${value?.name}'")
		        field?.dispose()
		        field = value
		        eventBus.post(CurrentLibraryEvent(field))
	        }
        }

    val library: Library get() = l!!

	/** ---- [MetaGraphRepository] */

	override fun getContainerLibraryElement(uuid: UUID): ContainerLibraryElement? =
		library.getContainerLibraryElement(uuid)

	override fun getMetaGraph(uuid: UUID): MetaGraph = library.getMetaGraph(uuid)

	override fun getMetaGraphUnwrapped(uuid: UUID): MetaGraph = library.getMetaGraphUnwrapped(uuid)

	override fun getOptionalMetaGraph(uuid: UUID): MetaGraph? = library.getOptionalMetaGraph(uuid)

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