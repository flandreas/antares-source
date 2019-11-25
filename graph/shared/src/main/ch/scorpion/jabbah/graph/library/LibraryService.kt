package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCloner
import ch.scorpion.jabbah.io.StorableCreator

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

/** Posted on [EventBus] when a [LibraryImpl] hast been moved within its [LibraryDirectory].*/
data class LibraryItemMovedEvent(
	val parent: LibraryDirectory,
	val item: LibraryItem,
	val index: Int
)

/** Posted on [EventBus] when a [Library] has been deleted.*/
data class LibraryDeletedEvent(val uuid: UUID)

/** Posted on [EventBus] when a [Library] has been renamed.*/
data class LibraryRenamedEvent(
	val library: Library,
	val oldName: String
)

/** Posted on [EventBus] when a [LibraryDirectory] has been renamed*/
data class LibraryDirectoryRenamedEvent(
	val directory: LibraryDirectory,
	val oldName: String
)

/**
 * Provides methods for accessing and manipulating a single [Library].
 * Implementations will use a [LibraryPersistenceService] to make these manipulations persistent.
 */
class LibraryService(
	private val libraryAccessor: () -> Library? = { LibraryModule.libraryHolder.library },
	private val persistenceService: LibraryPersistenceService = LibraryModule.libraryPersistenceService,
	private val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	companion object {
		private val LOG by logger(LibraryService::class)
	}

	/** ---- [LibraryService] interface */

	/** Returns the current [Library].*/
	val currentLibrary: Library? get() = libraryAccessor.invoke()

	/** Loads the [Library] with the specified [UUID] from persistent store.*/
	fun loadLibrary(uuid: UUID): Library {
		try {
			LOG.debug("Loading Library $uuid")
			val library = persistenceService.loadLibrary(uuid)
			library.bindLibraryItems()
			return library
		} catch (e: LibraryPersistenceServiceException) {
			throw IllegalArgumentException("library $uuid not found")
		}
	}

	/** Stores the specified [Library] in persistent store.*/
	fun storeLibrary(library: Library) {
		LOG.debug("Storing Library ${library.uuid}")
		persistenceService.storeLibrary(library)
	}

	/**
	 * Deletes a [Library] persistently.
	 *
	 * Posts a [LibraryDeletedEvent] on this [LibraryService]'s [EventBus].
	 * @throws IllegalArgumentException if a [Library] with the specified [UUID] doesn't exist
	 */
	fun deleteLibrary(uuid: UUID) {
		LOG.debug("Deleting Library $uuid")
		persistenceService.deleteLibrary(uuid)
		eventBus.post(LibraryDeletedEvent(uuid))
	}

	/**
	 * Silently deletes any resources related with a [Library].
	 * This is primarily used by higher-level services after importing an invalid [Library].
	 */
	fun purgeLibrary(uuid: UUID) {
		LOG.debug("Purging Library $uuid")
		persistenceService.deleteLibrary(uuid)
	}

	/** Renames a [Library] and makes the change persistent.*/
	fun renameLibrary(library: Library, newName: String) {
		LOG.debug("Renaming Library ${library.uuid} to '$newName'")
		val oldName = library.name
		library.name = newName
		storeLibrary(library)
		eventBus.post(LibraryRenamedEvent(library, oldName))
	}

	/**
	 * Adds a [LibraryItem] at the end position of a [LibraryDirectory] and makes the change persistent.
	 * Posts a [LibraryItemAddedEvent] on this [LibraryService]'s [EventBus].
	 */
	fun addLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory, index: Int? = null) {
		LOG.debug("LibraryServiceImpl: Adding LibraryItem ${item.name}'")
		item.bindTo(library)
		if (index != null) {
			directory.add(index, item)
		} else {
			directory.add(item)
		}
		storeLibrary(library)
		eventBus.post(LibraryItemAddedEvent(directory, item))
	}

	/**
	 * Removes a [LibraryItem] from a [LibraryDirectory] and makes the change persistent.
	 * Posts a [LibraryItemRemovedEvent] on this [LibraryService]'s [EventBus].
	 * @throws IllegalStateException is `item` is a non-empty [LibraryDirectory]
	 */
	fun removeLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory) {
		LOG.debug("Removing LibraryItem '${item.name}'")
		if (directory.remove(item)) {
			if (item is ContainerLibraryElement) {
				persistenceService.deleteContainerLibraryElement(library, item.uuid)
				if (library.defaultElementUUID == item.uuid) {
					library.defaultElementUUID = null
				}
			} else if (item is LibraryFolder) {
				if (!item.isEmpty()) {
					LOG.debug("LibraryServiceImpl: Refusing to delete non-empty LibraryFolder")
					throw IllegalStateException("can't delete non-empty LibraryFolder")
				}
			}
			item.dispose()
			storeLibrary(library)
			eventBus.post(LibraryItemRemovedEvent(directory, item))
		}
	}

	/**
	 * Creates a new [LibraryDirectory] with the specified name, adds it to the parent [LibraryDirectory],
	 * and makes the change persistent.
	 * Posts a [LibraryItemAddedEvent] on this [LibraryService]'s [EventBus].
	 * @return the created [LibraryDirectory]
	 */
	fun addFolder(library: Library, name: String, directory: LibraryDirectory): LibraryDirectory {
		LOG.debug("Adding new Folder '$name'")
		val folder = LibraryFolder(name)
		addLibraryItem(library, folder, directory)
		return folder
	}

	/**
	 * Moves a [LibraryItem] to a new position within its [LibraryDirectory].
	 * @param item the [LibraryItem] to be moved within its [LibraryDirectory]
	 * @param newIndex the new index of `item` within its [LibraryDirectory] after it has been moved
	 *
	 */
	fun move(library: Library, item: LibraryItem, newIndex: Int) {
		val directory = getDirectoryOf(library, item)
		directory.move(item, newIndex)
		storeLibrary(library)
		eventBus.post(LibraryItemMovedEvent(directory, item, newIndex))
	}

	/**
	 * Renames the specified [LibraryDirectory].
	 * Posts [LibraryDirectoryRenamedEvent] on this [LibraryService]'s [EventBus].
	 */
	fun renameDirectory(directory: LibraryDirectory, newName: TranslatableText) {
		LOG.debug("Renaming LibraryDirectory")
		val oldName = directory.name
		directory.translatableName = newName
		storeLibrary(directory.library!!)
		eventBus.post(LibraryDirectoryRenamedEvent(directory, oldName))
	}

	/**
	 * Creates a new [ContainerLibraryElement] for holding the specified [MetaGraph], add it to the [LibraryDirectory],
	 * and stores the [MetaGraph] as well as the [Library] persistently.
	 * Posts a [LibraryItemAddedEvent] on this [LibraryService]'s [EventBus].
	 * @return the created [ContainerLibraryElement]
	 */
	fun addContainerLibraryElement(library: Library, metaGraph: MetaGraph, directory: LibraryDirectory, index: Int? = null): ContainerLibraryElement {
		LOG.debug("Adding ContainerLibraryElement")
		val element = createContainerLibraryElement(metaGraph)
		storeContainerLibraryElement(library, metaGraph, element, doClone = true)
		addLibraryItem(library, element, directory, index)
		return element
	}

	/**
	 * Clones the specified [MetaGraph], uses it as the new [MetaGraph] of the specified [ContainerLibraryElement],
	 * and makes the change persistent.
	 * Posts a [LibraryItemUpdatedEvent] on this [LibraryService]'s [EventBus].
	 */
	fun updateContainerLibraryElement(library: Library, element: ContainerLibraryElement) {
		LOG.debug("Updating ContainerLibraryElement")
		element.metaGraph?.let {
			val nameChanged = it.translatableName != element.translatableName
			storeContainerLibraryElement(library, it, element, doClone = false)
			if (nameChanged) {
				element.translatableName = it.translatableName
				storeLibrary(library)
			}
			eventBus.post(LibraryItemUpdatedEvent(library, element))
		}
	}

	/**
	 * Returns the [MetaGraph] of a [ContainerLibraryElement], loading it if not already loaded.
	 */
	fun getMetaGraph(library: Library, element: ContainerLibraryElement): MetaGraph {
		ensureMetaGraph(library, element)
		return element.metaGraph!!
	}

	/**
	 * Reloads the [MetaGraph] of a [ContainerLibraryElement], loading it always, even if it is already loaded.
	 */
	fun loadMetaGraph(library: Library, element: ContainerLibraryElement) {
		ensureMetaGraph(library, element, loadAlways = true)
	}

	/**
	 * Sets the [UUID] of the [ContainerLibraryElement] to be opened when the [Library] is opened,
	 * and makes the change persistent.
	 */
	fun setDefaultElement(library: Library, uuid: UUID?) {
		if (library.defaultElementUUID != uuid) {
			LOG.debug("Setting default element to '$uuid'")
			library.defaultElementUUID = uuid
			storeLibrary(library)
		}
	}

	/**
	 * Returns the [LibraryDirectory] in [Library] that directly contains the specified [LibraryItem].
	 * @throws IllegalArgumentException if none is found
	 */
	fun getDirectoryOf(library: Library, item: LibraryItem): LibraryDirectory {
		val finder = LibraryItemFinder(item)
		library.accept(finder)
		if (finder.result != null) {
			return finder.result!!
		}
		LOG.debug("could't find owning LibraryDirectory of LibraryItem")
		throw IllegalStateException()
	}

	fun duplicateContainerLibraryElement(directory: LibraryDirectory, element: ContainerLibraryElement, newName: String): ContainerLibraryElement {
		LOG.debug("Duplicate ContainerLibraryElement")
		val duplicate = storableCloner.clone(element.metaGraph!!) as MetaGraph
		duplicate.graph.model!!.initializeUUID()
		duplicate.graph.model!!.name.value = newName
		return addContainerLibraryElement(directory.library!!, duplicate, directory)
	}

	/**
	 * Duplicates the specified [Library] and stores the duplicate with the given new name.
	 * @return the created duplicate [Library]
	 */
	fun duplicateLibrary(library: Library, newName: String): Library {
		LOG.debug("Duplicate Library ${library.uuid} to new name $newName")
		val newUuid = System.get().createUUID()
		persistenceService.duplicateLibrary(library, newUuid)
		val newLibrary = persistenceService.loadLibrary(newUuid)
		newLibrary.uuid = newUuid
		newLibrary.name = newName
		storeLibrary(newLibrary)
		return newLibrary
	}

	/**
	 * Exports the [Library] with the specified [UUID] by storing its entire contents at an output path.
	 * Note that this wouldn't work in a client/server setup, which would require the exported data to be
	 * transferred to the client to be stored there. This is up to a future extension.
	 */
	fun exportLibrary(uuid: UUID, outputPath: String) {
		persistenceService.exportLibrary(uuid, outputPath)
	}

	/**
	 * Imports a [Library] by reading its entire contents from the specified input path.
	 * Note that this wouldn't work in a client/server setup, which would require the imported data to be
	 * transferred to the server to be stored there. This is up to a future extension.
	 * @return the imported [Library], or `null` if the [Library] is invalid
	 */
	fun importLibrary(uuid: UUID, inputPath: String): Library? {
		persistenceService.importLibrary(uuid, inputPath)
		return try {
			loadLibrary(uuid)
		} catch (e: Throwable) {
			persistenceService.deleteLibrary(uuid)
			null
		}
	}

	private fun storeContainerLibraryElement(library: Library, metaGraph: MetaGraph, element: ContainerLibraryElement, doClone: Boolean) {
		LOG.debug("LibraryServiceImpl: Storing MetaGraph")
		if (doClone) {
			val clone = storableCloner.cloneUsingCreator(metaGraph, storableCreator) as MetaGraph
			element.updateMetaGraph(clone)
			persistenceService.storeMetaGraph(library, clone)
		} else {
			persistenceService.storeMetaGraph(library, metaGraph)
		}
	}

	private fun createContainerLibraryElement(metaGraph: MetaGraph): ContainerLibraryElement {
		return ContainerLibraryElement(
			uuid = metaGraph.uuid,
			name = metaGraph.name,
			iconPath = null,
			eventBus = eventBus)
	}

	private fun ensureMetaGraph(library: Library, element: ContainerLibraryElement, loadAlways: Boolean = false) {
		if (loadAlways || element.metaGraph == null) {
			element.updateMetaGraph(persistenceService.loadMetaGraph(library, element.uuid))
			LOG.debug("LibraryServiceImpl: Loaded MetaGraph '${element.metaGraph!!.name}' ${element.uuid}")
		}
	}

	/** Finds the [LibraryDirectory] that directly contains `item`.*/
	private class LibraryItemFinder(private val item: LibraryItem) : EmptyHierarchyVisitor() {
		var result: LibraryDirectory? = null

		override fun visitEnter(node: Any): Boolean {
			if (node is LibraryDirectory && node.contains(item)) {
				result = node
				return false
			}
			return true
		}
	}
}