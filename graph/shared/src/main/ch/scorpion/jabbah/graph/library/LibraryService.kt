package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCloner
import ch.scorpion.jabbah.io.StorableCreator

/**
 * Provides methods for accessing and manipulating a [Library].
 * Implementations will use a [LibraryPersistenceService] to make these manipulations persistent.
 */
interface LibraryService {

	/** Returns the current [Library].*/
	val currentLibrary: Library?

	/** Loads the [Library] with the specified name from persistent store.*/
	fun loadLibrary(name: String): Library

	/** Stores the specified [Library] in persistent store.*/
	fun storeLibrary(library: Library)

	/**
	 * Deletes a [Library] persistently.
	 * Posts a [LibraryDeletedEvent] on this [LibraryService]'s [EventBus].
	 * @throws IllegalArgumentException if a [Library] with the specified name doesn't exist
	 */
	fun deleteLibrary(name: String)

	/** Renames a [Library] and makes the change persistent.*/
	fun renameLibrary(library: Library, newName: String)

	/**
	 * Adds a [LibraryItem] at the end position of a [LibraryDirectory] and makes the change persistent.
	 * Posts a [LibraryItemAddedEvent] on this [LibraryService]'s [EventBus].
	 */
	fun addLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory, index: Int? = null)

	/**
	 * Removes a [LibraryItem] from a [LibraryDirectory] and makes the change persistent.
	 * Posts a [LibraryItemRemovedEvent] on this [LibraryService]'s [EventBus].
	 * @throws IllegalStateException is `item` is a non-empty [LibraryDirectory]
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
	 * Renames the specified [LibraryDirectory].
	 * Posts [LibraryDirectoryRenamedEvent] on this [LibraryService]'s [EventBus].
	 */
	fun renameDirectory(directory: LibraryDirectory, newName: TranslatableText)

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
	fun addContainerLibraryElement(library: Library, metaGraph: MetaGraph, directory: LibraryDirectory, index: Int? = null): ContainerLibraryElement

	/**
	 * Clones the specified [MetaGraph], uses it as the new [MetaGraph] of the specified [ContainerLibraryElement],
	 * and makes the change persistent.
	 * Posts a [LibraryItemUpdatedEvent] on this [LibraryService]'s [EventBus].
	 */
	fun updateContainerLibraryElement(library: Library, element: ContainerLibraryElement)

	/**
	 * Returns the [MetaGraph] of a [ContainerLibraryElement], loading it if not already loaded.
	 */
	fun getMetaGraph(library: Library, element: ContainerLibraryElement): MetaGraph

	/**
	 * Returns the [MetaGraph] of a [ContainerLibraryElement], loading it always, even if it is already loaded.
	 */
	fun loadMetaGraph(library: Library, element: ContainerLibraryElement): MetaGraph

	/** Sets the [UUID] of the [ContainerLibraryElement] to be opened when the [Library] is opened, and makes the change persistent.*/
	fun setDefaultElement(library: Library, uuid: UUID?)

	/**
	 * Returns the [LibraryDirectory] in [Library] that directly contains the specified [LibraryItem].
	 * @throws IllegalArgumentException if none is found
	 */
	fun getDirectoryOf(library: Library, item: LibraryItem): LibraryDirectory

	fun duplicateContainerLibraryElement(directory: LibraryDirectory, element: ContainerLibraryElement, newName: String): ContainerLibraryElement

	/**
	 * Duplicates the specified [Library] and stores the duplicate with the given new name.
	 * @return the created duplicate [Library]
	 */
	fun duplicateLibrary(library: Library, newName: String): Library
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

/** Posted on [EventBus] when a [Library] has been deleted.*/
data class LibraryDeletedEvent(
	val uuid: UUID,
	val name: String
)

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

class LibraryServiceImpl(
	private val libraryAccessor: () -> Library? = { LibraryModule.libraryHolder.library },
	private val persistenceService: LibraryPersistenceService = LibraryModule.libraryPersistenceService,
	private val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val eventBus: EventBus = BaseModule.eventBus
) : LibraryService {

	companion object {
		private val LOG by logger(LibraryServiceImpl::class)
	}

	/** ---- [LibraryService] interface */

	override val currentLibrary: Library? get() = libraryAccessor.invoke()

	override fun loadLibrary(name: String): Library {
		try {
			LOG.debug("LibraryServiceImpl: Loading Library '$name'")
			val library = persistenceService.loadLibrary(name)
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

	override fun deleteLibrary(name: String) {
		LOG.debug("LibraryServiceImpl: Deleting Library '$name'")
		val library = loadLibrary(name)
		persistenceService.deleteLibrary(name)
		eventBus.post(LibraryDeletedEvent(library.uuid, name))
	}

	override fun renameLibrary(library: Library, newName: String) {
		LOG.debug("LibraryServiceImpl: Renaming Library '${library.name}' to '$newName'")
		val oldName = library.name
		persistenceService.renameLibrary(library, newName)
		library.name = newName
		storeLibrary(library)
		eventBus.post(LibraryRenamedEvent(library, oldName))
	}

	override fun addLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory, index: Int?) {
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

	override fun removeLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory) {
		LOG.debug("LibraryServiceImpl: Removing LibraryItem '${item.name}'")
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

	override fun addFolder(library: Library, name: String, directory: LibraryDirectory): LibraryDirectory {
		LOG.debug("LibraryServiceImpl: Adding new Folder '$name'")
		val folder = LibraryFolder(name)
		addLibraryItem(library, folder, directory)
		return folder
	}

	override fun renameDirectory(directory: LibraryDirectory, newName: TranslatableText) {
		LOG.debug("LibraryServiceImpl: Renaming LibraryDirectory")
		val oldName = directory.name
		directory.translatableName = newName
		storeLibrary(directory.library!!)
		eventBus.post(LibraryDirectoryRenamedEvent(directory, oldName))
	}

	override fun ensureFolder(library: Library, name: String, directory: LibraryDirectory): LibraryDirectory {
		val item = directory.get(name)
		if (item != null) {
			return item as LibraryFolder
		}
		return addFolder(library, name, directory)
	}

	override fun addContainerLibraryElement(library: Library, metaGraph: MetaGraph, directory: LibraryDirectory, index: Int?): ContainerLibraryElement {
		LOG.debug("LibraryServiceImpl: Adding ContainerLibraryElement")
		val element = createContainerLibraryElement(metaGraph)
		storeContainerLibraryElement(library, metaGraph, element)
		addLibraryItem(library, element, directory, index)
		return element
	}

	override fun updateContainerLibraryElement(library: Library, element: ContainerLibraryElement) {
		LOG.debug("LibraryServiceImpl: Updating ContainerLibraryElement")
		element.metaGraph?.let {
			val nameChanged = it.name != element.name
			storeContainerLibraryElement(library, it, element)
			if (nameChanged) {
				storeLibrary(library)
			}
			eventBus.post(LibraryItemUpdatedEvent(library, element))
		}
	}

	override fun getMetaGraph(library: Library, element: ContainerLibraryElement): MetaGraph {
		ensureMetaGraph(library, element)
		return element.metaGraph!!
	}

	override fun loadMetaGraph(library: Library, element: ContainerLibraryElement): MetaGraph {
		ensureMetaGraph(library, element, loadAlways = true)
		return element.metaGraph!!
	}

	override fun setDefaultElement(library: Library, uuid: UUID?) {
		if (library.defaultElementUUID != uuid) {
			LOG.debug("LibraryServiceImpl: Setting default element to '$uuid'")
			library.defaultElementUUID = uuid
			storeLibrary(library)
		}
	}

	override fun getDirectoryOf(library: Library, item: LibraryItem): LibraryDirectory {
		val finder = LibraryItemFinder(item)
		library.accept(finder)
		if (finder.result != null) {
			return finder.result!!
		}
		LOG.debug("LibraryServiceImpl: could't find owning LibraryDirectory of LibraryItem")
		throw IllegalStateException()
	}

	override fun duplicateContainerLibraryElement(directory: LibraryDirectory, element: ContainerLibraryElement, newName: String): ContainerLibraryElement {
		LOG.debug("LibraryServiceImpl: Duplicate ContainerLibraryElement")
		val duplicate = storableCloner.clone(element.metaGraph!!) as MetaGraph
		duplicate.graph.model!!.initializeUUID()
		duplicate.graph.model!!.name = newName
		return addContainerLibraryElement(directory.library!!, duplicate, directory)
	}

	override fun duplicateLibrary(library: Library, newName: String): Library {
		LOG.debug("LibraryServiceImpl: Duplicate Library ${library.name} to new name $newName")
		persistenceService.duplicateLibrary(library, newName)
		val duplicateLibrary = persistenceService.loadLibrary(newName)
		duplicateLibrary.uuid = System.get().createUUID()
		duplicateLibrary.name = newName
		storeLibrary(duplicateLibrary)
		return duplicateLibrary
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