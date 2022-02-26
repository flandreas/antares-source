package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.UserIdentity
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.*
import ch.scorpion.jabbah.graph.module.GraphModule
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCloner
import ch.scorpion.jabbah.io.StorableCreator
import kotlin.math.min

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

/** Posted on [EventBus] when a [LibraryImpl] has been within the same or to another [LibraryDirectory].*/
data class LibraryItemMovedEvent(
	val oldDirectory: LibraryDirectory,
	val item: LibraryItem,
	val newDirectory: LibraryDirectory,
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

data class ContainerLibraryElementRenamedEvent(
	val element: ContainerLibraryElement,
	val oldName: TranslatableText
)

enum class MetaGraphBundleImportResult {
	Success,
	Invalid,
	StaleLibraryReference,
	UuidAlreadyExists
}

/**
 * Provides methods for accessing and manipulating a single [Library].
 * Implementations will use a [LibraryPersistenceService] to make these manipulations persistent.
 */
class LibraryService(
	private val userLibraryPersister: LibraryPersistenceService = LibraryModule.userLibraryPersistenceService,
	private val systemLibraryPersister: LibraryPersistenceService = LibraryModule.systemLibraryPersistenceService,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val metaGraphRepository: CombinedMetaGraphRepository = GraphModule.metaGraphRepository
) {

	companion object {
		private val LOG by logger(LibraryService::class)
	}

	/** ---- [LibraryService] interface */

	private fun persister(system: Boolean): LibraryPersistenceService =
		if (system) systemLibraryPersister else userLibraryPersister

	/** Loads the [Library] with the specified [LibraryIdentification] from persistent store.*/
	fun loadLibrary(libraryId: LibraryIdentification, isSystem: Boolean): Library {
		LOG.trace("Loading Library $${libraryId.uuid}")
		val library = persister(isSystem).loadLibrary(libraryId)
		library.bindLibraryItems()
		return library
	}

	/** Stores the specified [Library] in persistent store.*/
	fun storeLibrary(library: Library) {
		LOG.trace("Storing Library ${library.uuid} with ID ${library.hashCode()}")
		persister(library.isSystem).storeLibrary(library)
	}

	/**
	 * Deletes a user [Library] persistently.
	 *
	 * Posts a [LibraryDeletedEvent] on this [LibraryService]'s [EventBus].
	 * @throws IllegalArgumentException if a [Library] with the specified [UUID] doesn't exist
	 */
	fun deleteLibrary(libraryId: LibraryIdentification) {
		LOG.trace("Deleting Library $${libraryId.uuid}")
		userLibraryPersister.deleteLibrary(libraryId)
		eventBus.post(LibraryDeletedEvent(libraryId.uuid))
	}

	/**
	 * Silently deletes any resources related with a user [Library].
	 * This is primarily used by higher-level services after importing an invalid [Library].
	 */
	fun purgeLibrary(libraryId: LibraryIdentification) {
		LOG.trace("Purging Library $${libraryId.uuid}")
		userLibraryPersister.deleteLibrary(libraryId)
	}

	/** Renames a [Library] and makes the change persistent.*/
	fun renameLibrary(library: Library, newName: TranslatableText) {
		LOG.trace("Renaming Library ${library.uuid} to '$newName'")
		val oldName = library.name.translation
		library.name = Name(newName)
		storeLibrary(library)
		eventBus.post(LibraryRenamedEvent(library, oldName))
	}

	/**
	 * Adds a [LibraryItem] at the specified index of a [LibraryDirectory] and makes the change persistent.
	 * Posts a [LibraryItemAddedEvent] on this [LibraryService]'s [EventBus].
	 * @param index the position within [directory] to add [item], or `null` to add at the end
	 */
	fun addLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory, index: Int? = null) {
		LOG.trace("Adding LibraryItem ${item.name}'")
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
		LOG.trace("Removing LibraryItem '${item.name}'")
		if (directory.remove(item)) {
			if (item is ContainerLibraryElement) {
				LOG.trace("Delete MetaGraph ${item.uuid}")
				persister(library.isSystem).deleteMetaGraph(library, item.uuid)
				if (library.defaultElementUUID == item.uuid) {
					library.defaultElementUUID = null
				}
			} else if (item is LibraryFolder) {
				if (!item.isEmpty()) {
					LOG.trace("Refusing to delete non-empty LibraryFolder")
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
		LOG.debug("Add new folder '${name.getOptionalTranslation()}' in '${directory.name}'")
		val folder = LibraryFolder(name)
		addLibraryItem(library, folder, directory)
		return folder
	}

	/**
	 * Moves a [LibraryItem] (which can also be a [LibraryFolder]) to another [LibraryDirectory].
	 */
	fun move(library: Library, item: LibraryItem, destination: LibraryDirectory, newIndex: Int?) {
		val origDirectory = getDirectoryOf(library, item)

		origDirectory.remove(item)

		val effNewIndex = newIndex?.let { min(it, destination.size) } ?: destination.size
		destination.add(effNewIndex, item)

		eventBus.post(LibraryItemMovedEvent(origDirectory, item, destination, effNewIndex))
		storeLibrary(library)
	}

	/**
	 * Renames the specified [LibraryDirectory].
	 * Posts [LibraryDirectoryRenamedEvent] on this [LibraryService]'s [EventBus].
	 */
	fun renameDirectory(directory: LibraryDirectory, newName: TranslatableText) {
		LOG.debug("Rename folder '${directory.name}' to '${newName.getOptionalTranslation()}'")
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
		LOG.trace("Adding ContainerLibraryElement")
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
		LOG.trace("Updating ContainerLibraryElement")
		element.metaGraph?.let {
			val nameChanged = it.translatableName != element.name.translation
			storeContainerLibraryElement(library, it, element, doClone = false)
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
			LOG.debug("Set default element to $uuid")
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
		LOG.error("couldn't find owning LibraryDirectory of LibraryItem")
		throw IllegalStateException()
	}

	fun duplicateContainerLibraryElement(directory: LibraryDirectory, element: ContainerLibraryElement, newName: TranslatableText): ContainerLibraryElement {
		LOG.debug("Duplicate '${element.metaGraph?.uuid}' with name '${element.metaGraph?.name}'")
		val duplicate = element.metaGraph!!.duplicate(newName)
		return addContainerLibraryElement(directory.library!!, duplicate, directory)
	}

	fun renameContainerLibraryElement(element: ContainerLibraryElement, newName: TranslatableText) {
		LOG.debug("Renaming '${element.metaGraph?.uuid} to '${newName.getTranslation()}'")
		val oldName = element.name.translation
		val name = Name(newName)
		element.metaGraph!!.graph.model!!.name = name
		element.name = name

		persister(element.library!!.isSystem).storeMetaGraph(element.library!!, element.metaGraph!!)
		storeLibrary(element.library!!)

		eventBus.post(ContainerLibraryElementRenamedEvent(element, oldName))
	}

	/**
	 * Duplicates the specified [Library] (either user or system [Library]) and store the duplicate with
	 * the given new name as user [Library].
	 * @param library the [Library] to duplicate
	 * @param newName the name of the duplicate [Library]
	 * @param owner the [UserIdentity] of the user who owns the duplicated [Library]
	 */
	fun duplicateLibrary(library: Library, newName: TranslatableText, owner: UserIdentity): Library {
		val newUuid = System.createUUID()
		LOG.debug("Duplicate Library ${library.uuid} to new name '${newName.getOptionalTranslation()}' and UUID $newUuid")

		val path = persister(library.isSystem).exportLibraryTemporarily(library.identification)

		val newIdentification = LibraryIdentification(newUuid, owner)
		userLibraryPersister.importTemporaryLibrary(newIdentification, path)
		val newLibrary = loadLibrary(newIdentification, isSystem = false)

		newLibrary.uuid = newUuid
		newLibrary.name = Name(newName)
		newLibrary.isSystem = false
		newLibrary.author = owner

		storeLibrary(newLibrary)

		return newLibrary
	}

	/**
	 * Exports the user [Library] with the specified [UUID] by storing its entire contents at an output path.
	 * Note that this wouldn't work in a client/server setup, which would require the exported data to be
	 * transferred to the client to be stored there. This is up to a future extension.
	 */
	fun exportLibrary(libraryId: LibraryIdentification, outputPath: String) {
		userLibraryPersister.exportLibrary(libraryId, outputPath)
	}

	/**
	 * Imports a [Library] by reading its entire contents from the specified input path.
	 * Note that this wouldn't work in a client/server setup, which would require the imported data to be
	 * transferred to the server to be stored there. This is up to a future extension.
	 *
	 * @throws kotlin.IllegalArgumentException if the import file could not be read successfully
	 * @throws LibraryImportConflictException if a [Library] with the same [UUID] already exists
	 * @throws GraphQuotaException if the user's [GraphQuota] are not sufficient to import the [Library]
	 * @return the imported [Library], or `null` if the [Library] is invalid
	 */
	fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library =
		userLibraryPersister.importLibrary(inputPath, currentLibraryCount, quota)

	/**
	 * Creates a [MetaGraphBundle] for the [MetaGraph] in [element] and stores it as a ZIP file
	 * at the location [outputPath].
	 */
	fun exportMetaGraphBundle(element: ContainerLibraryElement, outputPath: String) {
		ensureMetaGraph(element.library!!, element)
		val bundle = metaGraphRepository.createBundle(element.metaGraph!!)
		userLibraryPersister.exportMetaGraphBundle(bundle, outputPath)
	}

	fun importMetaGraphBundle(
		inputPath: String,
		bundleName: String,
		destination: LibraryDirectory,
		replaceIfUuidExists: Boolean
	): MetaGraphBundleImportResult {
		lateinit var bundle: MetaGraphBundle

		try {
			bundle = userLibraryPersister.importMetaGraphBundle(inputPath)
		} catch (e: Throwable) {
			return MetaGraphBundleImportResult.Invalid
		}

		if (!checkBundleLibrary(bundle, destination.library!!)) {
			return MetaGraphBundleImportResult.StaleLibraryReference
		}

		val conflict = anyBundleUuidExists(bundle)
		if (conflict && !replaceIfUuidExists) {
			return MetaGraphBundleImportResult.UuidAlreadyExists
		}

		if (conflict) {
			deleteBundleMetaGraphs(bundle, destination.library!!)
		}

		importMetaGraphBundle(bundle, bundleName, destination)

		return MetaGraphBundleImportResult.Success
	}

	/** Returns the code snippet to embed the [MetaGraph] with [UUID] as a HTML <iframe>.*/
	fun getEmbeddingIFrame(uuid: UUID): String {
		val metaGraph = metaGraphRepository.getMetaGraph(uuid)
		val library = metaGraphRepository.getContainingLibrary(uuid)!!
		val src = StringBuilder(BaseModule.properties.getString(DataLocation.PROP_SERVER_URL))
			.append("/jabbah/iframe/iframe.html?")
			.append("project=${library.uuid.id}")
			.append("&owner=${library.author.id}")
			.append("&circuit=${uuid.id}")
			.toString()

		return """
			|<iframe
			|   style="border:none;"
			|   title="${metaGraph.name}"
			|   width="500px"
			|   height="500px"
			|   src="$src"
			|/>
		""".trimMargin()
	}

	private fun anyBundleUuidExists(bundle: MetaGraphBundle): Boolean =
		bundle.metaGraphs.any { metaGraphRepository.containsMetaGraph(it.uuid) }

	private fun checkBundleLibrary(bundle: MetaGraphBundle, destination: Library): Boolean {
		if (bundle.referencedSystemLibrary == null) {
			return true
		}
		if (destination is Project) {
			return bundle.referencedSystemLibrary == destination.importedLibrary
		}
		return bundle.referencedSystemLibrary == destination.uuid
	}

	private fun importMetaGraphBundle(bundle: MetaGraphBundle, bundleName: String, destination: LibraryDirectory) {
		val bundleDirectory = addFolder(destination.library!!, TranslatableText("Import '$bundleName'"), destination)
		bundle.metaGraphs.forEach {
			addContainerLibraryElement(destination.library!!, it, bundleDirectory)
		}
	}

	private fun deleteBundleMetaGraphs(bundle: MetaGraphBundle, library: Library) {
		bundle.metaGraphs.forEach { metaGraph ->
			library.getContainerLibraryElement(metaGraph.uuid)?.let {
				LOG.trace("Replace MetaGraph ${metaGraph.uuid}")
				removeLibraryItem(library, it, getDirectoryOf(library, it))
			}
		}
	}

	/**
	 * Stores a [MetaGraph] in a [Library] and updates the instance in the specified [ContainerLibraryElement].
	 */
	private fun storeContainerLibraryElement(library: Library, metaGraph: MetaGraph, element: ContainerLibraryElement, doClone: Boolean) {
		LOG.trace("Storing MetaGraph")
		if (doClone) {
			val clone = StorableCloner.cloneUsingCreator(metaGraph, storableCreator)
			element.updateMetaGraph(clone)
			persister(library.isSystem).storeMetaGraph(library, clone)
		} else {
			persister(library.isSystem).storeMetaGraph(library, metaGraph)
		}
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
			LOG.trace("Loaded MetaGraph $ref with ID ${metaGraph.hashCode()} from Library with ID ${library.hashCode()}")
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