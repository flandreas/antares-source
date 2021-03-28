package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.UserHolder
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.Storable

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
	val oldName: TranslatableText
)

/** Posted on [EventBus] when a [LibraryDirectory] has been renamed*/
data class LibraryDirectoryRenamedEvent(
	val directory: LibraryDirectory,
	val oldName: TranslatableText
)

/**
 * Provides methods for accessing and manipulating a single [Library].
 * Implementations will use a [LibraryPersistenceService] to make these manipulations persistent.
 */
class LibraryService(
	private val libraryAccessor: () -> Library? = { LibraryModule.libraryHolder.library },
	private val userLibraryPersister: LibraryPersistenceService = LibraryModule.userLibraryPersistenceService,
	private val systemLibraryPersister: LibraryPersistenceService = LibraryModule.systemLibraryPersistenceService,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val userHolder: UserHolder = EditAuthModule.userHolder
) {

	companion object {
		private val LOG by logger(LibraryService::class)
	}

	/** ---- [LibraryService] interface */

	/** Returns the current [Library].*/
	val currentLibrary: Library? get() = libraryAccessor.invoke()

	private fun persister(system: Boolean): LibraryPersistenceService =
		if (system) systemLibraryPersister else userLibraryPersister

	/** Loads the [Library] with the specified [UUID] from persistent store.*/
	fun loadLibrary(uuid: UUID, isSystem: Boolean): Library {
		LOG.debug("Loading Library $uuid")
		val library = persister(isSystem).loadLibrary(uuid)
		library.bindLibraryItems()
		return library
	}

	/** Stores the specified [Library] in persistent store.*/
	fun storeLibrary(library: Library) {
		LOG.debug("Storing Library ${library.uuid} with ID ${library.hashCode()}")
		persister(library.isSystem).storeLibrary(library)
	}

	/**
	 * Deletes a user [Library] persistently.
	 *
	 * Posts a [LibraryDeletedEvent] on this [LibraryService]'s [EventBus].
	 * @throws IllegalArgumentException if a [Library] with the specified [UUID] doesn't exist
	 */
	fun deleteLibrary(uuid: UUID) {
		LOG.debug("Deleting Library $uuid")
		userLibraryPersister.deleteLibrary(uuid)
		eventBus.post(LibraryDeletedEvent(uuid))
	}

	/**
	 * Silently deletes any resources related with a user [Library].
	 * This is primarily used by higher-level services after importing an invalid [Library].
	 */
	fun purgeLibrary(uuid: UUID) {
		LOG.debug("Purging Library $uuid")
		userLibraryPersister.deleteLibrary(uuid)
	}

	/** Renames a [Library] and makes the change persistent.*/
	fun renameLibrary(library: Library, newName: TranslatableText) {
		LOG.debug("Renaming Library ${library.uuid} to '$newName'")
		val oldName = library.name.translation
		library.name = Name(newName)
		storeLibrary(library)
		eventBus.post(LibraryRenamedEvent(library, oldName))
	}

	/**
	 * Adds a [LibraryItem] at the end position of a [LibraryDirectory] and makes the change persistent.
	 * Posts a [LibraryItemAddedEvent] on this [LibraryService]'s [EventBus].
	 */
	fun addLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory, index: Int? = null) {
		LOG.debug("Adding LibraryItem ${item.name}'")
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
				persister(library.isSystem).deleteMetaGraph(library, item.uuid)
				if (library.defaultElementUUID == item.uuid) {
					library.defaultElementUUID = null
				}
			} else if (item is LibraryFolder) {
				if (!item.isEmpty()) {
					LOG.debug("Refusing to delete non-empty LibraryFolder")
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
	fun addFolder(library: Library, name: TranslatableText, directory: LibraryDirectory): LibraryDirectory {
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
		val oldName = directory.name.translation
		directory.name = Name(newName)
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
		storeContainerLibraryElement(library, metaGraph, element)
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
			val nameChanged = it.translatableName != element.name.translation
			storeContainerLibraryElement(library, it, element)
			if (nameChanged) {
				element.name = Name(it.translatableName)
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
		LOG.debug("couldn't find owning LibraryDirectory of LibraryItem")
		throw IllegalStateException()
	}

	fun duplicateContainerLibraryElement(directory: LibraryDirectory, element: ContainerLibraryElement, newName: TranslatableText): ContainerLibraryElement {
		LOG.info("Duplicate ContainerLibraryElement ${element.metaGraph?.uuid} with name '${element.metaGraph?.name}'")
		val duplicate = element.metaGraph!!.duplicate(newName)
		return addContainerLibraryElement(directory.library!!, duplicate, directory)
	}

	/**
	 * Duplicates the specified user [Library] and stores the duplicate with the given new name.
	 * @return the created duplicate user [Library]
	 */
	fun duplicateLibrary(library: Library, newName: TranslatableText): Library {
		LOG.debug("Duplicate Library ${library.uuid} to new name '${newName.getTranslation()}'")
		val newUuid = System.createUUID()
		userLibraryPersister.duplicateLibrary(library, newUuid)
		val newLibrary = userLibraryPersister.loadLibrary(newUuid)
		newLibrary.uuid = newUuid
		newLibrary.name = Name(newName)
		newLibrary.isSystem = false
		newLibrary.author = userHolder.user.uuid
		storeLibrary(newLibrary)
		return newLibrary
	}

	/**
	 * Exports the user [Library] with the specified [UUID] by storing its entire contents at an output path.
	 * Note that this wouldn't work in a client/server setup, which would require the exported data to be
	 * transferred to the client to be stored there. This is up to a future extension.
	 */
	fun exportLibrary(uuid: UUID, outputPath: String) {
		userLibraryPersister.exportLibrary(uuid, outputPath)
	}

	/**
	 * Imports a [Library] by reading its entire contents from the specified input path.
	 * Note that this wouldn't work in a client/server setup, which would require the imported data to be
	 * transferred to the server to be stored there. This is up to a future extension.
	 * @return the imported [Library], or `null` if the [Library] is invalid
	 */
	fun importLibrary(inputPath: String): Library? {
		val uuid = userLibraryPersister.importLibrary(inputPath)
		return try {
			loadLibrary(uuid, isSystem = false)
		} catch (e: Throwable) {
			userLibraryPersister.deleteLibrary(uuid)
			null
		}
	}

	/**
	 * Stores a [MetaGraph] in a [Library] and updates the instance it in the specified [ContainerLibraryElement].
	 * [MetaGraph]s must be cloneable while preserving their persistent [Storable.storableId]s, which is only possible
	 * if these IDs already exist. Since these IDs are assigned when reading [Storable], storing a [ContainerLibraryElement]
	 * must involve reading it back from persistent store after storing.
	 */
	private fun storeContainerLibraryElement(library: Library, metaGraph: MetaGraph, element: ContainerLibraryElement) {
		LOG.debug("Storing MetaGraph with ID ${metaGraph.hashCode()} in Library with ID ${library.hashCode()}")
		persister(library.isSystem).storeMetaGraph(library, metaGraph)
		val clone = persister(library.isSystem).loadMetaGraph(library, metaGraph.uuid)
		element.updateMetaGraph(clone)
	}

	private fun createContainerLibraryElement(metaGraph: MetaGraph): ContainerLibraryElement {
		return ContainerLibraryElement(
			uuid = metaGraph.uuid,
			initialName = metaGraph.translatableName,
			iconPath = null,
			eventBus = eventBus)
	}

	private fun ensureMetaGraph(library: Library, element: ContainerLibraryElement, loadAlways: Boolean = false) {
		if (loadAlways || element.metaGraph == null) {
			val ref = "'${element.metaGraph?.name}' ${element.uuid}"
			val metaGraph = persister(library.isSystem).loadMetaGraph(library, element.uuid)
			LOG.debug("Loaded MetaGraph $ref with ID ${metaGraph.hashCode()} from Library with ID ${library.hashCode()}")
			element.updateMetaGraph(metaGraph)
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