package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCloner
import ch.scorpion.jabbah.io.StorableCreator

interface LibraryService {

	fun loadLibrary(name: String): Library

	fun loadLibrary(library: Library): Library

	fun storeLibrary(library: Library)

	/**
	 * Adds a [LibraryItem] at the end position of a [LibraryDirectory] and makes the change persistent.
	 * Posts a [LibraryItemAddedEvent] on this [LibraryService]'s [EventBus].
	 */
	fun addLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory)

	/**
	 * Removes a [LibraryItem] from a [LibraryDirectory] and makes the change persistent.
	 * Posts a [LibraryItemRemovedEvent] on this [LibraryService]'s [EventBus].
	 */
	fun removeLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory)

	/**
	 * Creates a new [LibraryDirectory] with the specified name, adds it to the parent [LibraryDirectory],
	 * and makes the change persistent.
	 * Posts a [LibraryItemAddedEvent] on this [LibraryService]'s [EventBus].
	 * @return the created [LibraryDirectory]
	 */
	fun addFolder(library: Library, name: String, directory: LibraryDirectory): LibraryDirectory

	/**
	 * Ensures that a [LibraryDirectory] contains a [LibraryFolder] with the specified name. Creates and adds a
	 * [LibraryFolder] in [directory] if it doesn't exist yet.
	 * @return the [LibraryFolder] within [directory] with name [name], which might just has been created.
	 */
	fun ensureFolder(library: Library, name: String, directory: LibraryDirectory): LibraryDirectory

	/**
	 * Creates a new [ContainerLibraryElement] for holding the specified [MetaGraph], add it to the [LibraryDirectory],
	 * and stores the [MetaGraph] as well as the [Library] persistently.
	 * Posts a [LibraryItemAddedEvent] on this [LibraryService]'s [EventBus].
	 * @return the created [ContainerLibraryElement]
	 */
	fun addContainerLibraryElement(library: Library, metaGraph: MetaGraph, directory: LibraryDirectory): ContainerLibraryElement

	/**
	 * Clones the specified [MetaGraph], uses it as the new [MetaGraph] of the specified [ContainerLibraryElement],
	 * and makes the change persistent.
	 * Posts a [LibraryItemUpdatedEvent] on this [LibraryService]'s [EventBus].
	 */
	fun updateContainerLibraryElement(library: Library, metaGraph: MetaGraph, element: ContainerLibraryElement)

	/**
	 * Returns the [MetaGraph] of a [ContainerLibraryElement], loading it if not already loaded.
	 */
	fun getMetaGraph(library: Library, element: ContainerLibraryElement): MetaGraph
}

/** Posted on [EventBus] when a [LibraryItem] has been added to a [LibraryDirectory].*/
data class LibraryItemAddedEvent(
	val parent: LibraryDirectory,
	val item: LibraryItem
)

/** Posted on [EventBus] when a [LibraryItem] has been removed from a [LibraryDirectory].*/
data class LibraryItemRemovedEvent(
	val parent: LibraryDirectory,
	val item: LibraryItem
)

/** Posted on [EventBus] when a [LibraryItem] has been updated in a [LibraryDirectory].*/
data class LibraryItemUpdatedEvent(
	val library: Library,
	val item: LibraryItem
)

class LibraryServiceImpl(
	private val persistenceService: LibraryPersistenceService = LibraryModule.libraryPersistenceService,
	private val libraryFactory: (String) -> Library = LibraryModule.libraryFactory,
	private val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val eventBus: EventBus = BaseModule.eventBus
) : LibraryService {

	companion object {
		private val LOG by logger(LibraryServiceImpl::class)
	}


	/** ---- [LibraryService] interface */

	override fun loadLibrary(name: String): Library {
		return loadLibrary(libraryFactory.invoke(name))
	}

	override fun loadLibrary(library: Library): Library {
		try {
			LOG.debug("LibraryServiceImpl: Loading Library '${library.name}'")
			persistenceService.loadLibrary(library)
			library.bindLibraryItems()
			return library
		} catch(e: LibraryPersistenceServiceException) {
			throw IllegalArgumentException("library not found")
		}
	}

	override fun storeLibrary(library: Library) {
		LOG.debug("LibraryServiceImpl: Storing Library")
		persistenceService.storeLibrary(library)
	}

	override fun addLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory) {
		LOG.debug("LibraryServiceImpl: Adding LibraryItem")
		item.bindTo(library)
		directory.add(item)
		storeLibrary(library)
		eventBus.post(LibraryItemAddedEvent(directory, item))
	}

	override fun removeLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory) {
		LOG.debug("LibraryServiceImpl: Removing LibraryItem")
		if (directory.remove(item)) {
			if (item is ContainerLibraryElement) {
				persistenceService.deleteContainerLibraryElement(library, item.uuid)
			} else if (item is LibraryFolder) {
				throw UnsupportedOperationException("removing of entire LibraryFolders is not yet implemented")
			}
			item.dispose()
			storeLibrary(library)
			eventBus.post(LibraryItemRemovedEvent(directory, item))
		}
	}

	override fun addFolder(library: Library, name: String, directory: LibraryDirectory): LibraryDirectory {
		LOG.debug("LibraryServiceImpl: Adding new Folder")
		val folder = LibraryFolder(name)
		addLibraryItem(library, folder, directory)
		return folder
	}

	override fun ensureFolder(library: Library, name: String, directory: LibraryDirectory): LibraryDirectory {
		val item = directory.get(name)
		if (item != null) {
			return item as LibraryFolder
		}
		return addFolder(library, name, directory)
	}

	override fun addContainerLibraryElement(library: Library, metaGraph: MetaGraph, directory: LibraryDirectory): ContainerLibraryElement {
		LOG.debug("LibraryServiceImpl: Adding ContainerLibraryElement")
		val element = createContainerLibraryElement(metaGraph)
		storeContainerLibraryElement(library, metaGraph, element)
		addLibraryItem(library, element, directory)
		return element
	}

	override fun updateContainerLibraryElement(library: Library, metaGraph: MetaGraph, element: ContainerLibraryElement) {
		LOG.debug("LibraryServiceImpl: Updating ContainerLibraryElement")
		storeContainerLibraryElement(library, metaGraph, element)
		eventBus.post(LibraryItemUpdatedEvent(library, element))
	}

	override fun getMetaGraph(library: Library, element: ContainerLibraryElement): MetaGraph {
		ensureMetaGraph(library, element)
		return element.metaGraph!!
	}

	/** ---- [LibraryServiceImpl] */

	private fun storeContainerLibraryElement(library: Library, metaGraph: MetaGraph, element: ContainerLibraryElement) {
		LOG.debug("LibraryServiceImpl: Storing MetaGraph")
		val clone = storableCloner.cloneUsingCreator(metaGraph, storableCreator) as MetaGraph
		element.updateMetaGraph(clone)
		persistenceService.storeMetaGraph(library, metaGraph)
	}

	private fun createContainerLibraryElement(metaGraph: MetaGraph): ContainerLibraryElement {
		return ContainerLibraryElement(
			uuid = metaGraph.uuid,
			name = metaGraph.name,
			iconPath = null,
			eventBus = eventBus)
	}

	private fun ensureMetaGraph(library: Library, element: ContainerLibraryElement) {
		if (element.metaGraph == null) {
			element.updateMetaGraph(persistenceService.loadMetaGraph(library, element.uuid))
			LOG.debug("LibraryServiceImpl: Loaded MetaGraph '${element.metaGraph!!.name}' ${element.uuid}")
		}
	}
}