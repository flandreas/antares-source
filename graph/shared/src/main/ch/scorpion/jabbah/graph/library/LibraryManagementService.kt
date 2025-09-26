package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.io.StorableCloner

/**
 * Posted on [EventBus] when a [Library] is to be opened and is to replace the currently open [Library].
 * Subscribers for this event can raise their veto, such as the application class that keeps track of
 * changes of the current [Library]'s or current project's open [MetaGraph].
 */
data class OpenLibraryRequest(val library: Library)

class CloseLibraryRequest

/**
 * Posted on [EventBus] when a new [Library] has been created
 * @property library the created [Library]
 */
data class LibraryCreatedEvent(val library: Library)

/**
 * Posted by domain services on [EventBus] when the entire [LibraryProperties] have been changed.
 */
data class LibraryPropertiesEvent(val library: Library, val properties: LibraryProperties)

/**
 * Posted by [LibraryManagementService] if [Library.importedLibraryIds] has been changed.
 */
data class LibraryImportsEvent(val library: Library)

/**
 * Provides methods for managing multiple [Libraries][Library].
 */
class LibraryManagementService(
	private val libraryFactory: LibraryFactory = LibraryModule.libraryFactory,
	libraryService: LibraryService = LibraryModule.libraryService,
	libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	userDictionaryService: LibraryDictionaryService = LibraryModule.userLibraryDictionaryService,
	systemDictionaryService: LibraryDictionaryService = LibraryModule.systemLibraryDictionaryService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryManagementService(
	libraryHolder,
	libraryService,
	userDictionaryService,
	userDictionaryService,
	systemDictionaryService, eventBus
) {

	companion object {
		private val LOG by logger(LibraryManagementService::class)
	}


	/** ---- [AbstractLibraryManagementService] */

	override fun existsName(name: TranslatableText, except: UUID?): Boolean =
		userDictionaryService.existsName(name, except)

	/** ---- [LibraryManagementService] */

	private fun isSystemLibrary(uuid: UUID): Boolean =
		systemDictionaryService.contains(uuid)

	/** Determines whether [uuid] exists as the [UUID] of either a user or a system [Library]. */
	fun contains(uuid: UUID): Boolean
		= systemDictionaryService.contains(uuid) || userDictionaryService.contains(uuid)

	/** Returns all [LibraryDictionaryEntries][LibraryDictionaryEntry].*/
	fun getLibraryDirectoryEntries(): ImmutableList<LibraryDictionaryEntry> {
		return listOf(
			userDictionaryService.getEntries(),
			systemDictionaryService.getEntries()
		).flatten().toImmutableList()
	}

	fun getOptionalLibrary(uuid: UUID): Library? {
		val dictionaryEntry = systemDictionaryService.getEntry(uuid) ?: userDictionaryService.getEntry(uuid)
		return dictionaryEntry?.let {
			loadLibrary(LibraryIdentification(it.uuid, it.author))
		}
	}

	/**
	 * Creates a new user [Library] with the given name and stores it in persistent store.
	 * Posts a [LibraryCreatedEvent] on [EventBus].
	 * @param properties the initial properties of the new [Library]
	 * @param importedLibraryId the [LibraryIdentification] of the [Library] to be imported,
	 *      or `null` if a standalone [Library] is to be created.
	 * @return the created [Library]
	 * @throws IllegalArgumentException if a [Library] with the name in [properties] already exists
	 */
	fun create(properties: LibraryProperties, importedLibraryId: LibraryIdentification?): Library {
		if (existsName(properties.name)) {
			throw IllegalArgumentException("library name '${properties.name.getTranslation()}' already exists")
		}
		LOG.userTrail("Create new library '${properties.name.getTranslation()}' with import '${importedLibraryId?.uuid}'")

		val library = libraryFactory.createEmptyLibrary(properties, importedLibraryId)

		library.bindLibraryItems()
		userDictionaryService.add(library)
		libraryService.storeLibrary(library)
		eventBus.post(LibraryCreatedEvent(library))

		return library
	}

	/**
	 * Updates the specified [Library] with the specified properties and stores it in persistent store.
	 * @throws IllegalArgumentException if [properties] are not consistent, e.g. if a [Library]
	 * with the specified name already exists.
	 * @throws IllegalStateException if no [Library] is currently open
	 * Posts [LibraryPropertiesEvent] on this [LibraryManagementService]'s [EventBus].
	 */
	fun update(library: Library, properties: LibraryProperties) {
		LOG.userTrail("Updating library ${library.uuid}")

		if (library.name.translation != properties.name) {
			if (existsName(properties.name, except = library.uuid)) {
				throw IllegalArgumentException("Library name '${properties.name.getTranslation()}' already exists")
			}
			libraryService.renameLibrary(library, properties.name)
			userDictionaryService.rename(library, properties.name)
		}

		library.properties = properties
		libraryService.storeLibrary(library)
		userDictionaryService.update(library, properties)
		eventBus.post(LibraryPropertiesEvent(library, properties))
	}

	/** Loads the [Library] with the specified [LibraryIdentification] from persistent store.*/
	fun loadLibrary(libraryId: LibraryIdentification): Library =
		libraryService.loadLibrary(libraryId, isSystemLibrary(libraryId.uuid))

	override fun open(libraryId: LibraryIdentification): Library =
		loadLibrary(libraryId).also { open(it) }

	fun open(library: Library) {
		if (libraryHolder.l == null) {
			openImpl(library)
		} else {
			eventBus.postVetoable(
				event = OpenLibraryRequest(library),
				undoEvent = OpenLibraryRequest(libraryHolder.library),
				thenHandler = {
					openImpl(library)
				}
			)
		}
	}

	/** Opens the specified [Library], while closing a currently open project*/
	fun open(libraryId: LibraryIdentification, containerLibraryElement: UUID? = null) {
		val library = loadLibrary(libraryId)
		LOG.trace("open library ${library.uuid}")
		if (libraryHolder.l == null) {
			//libraryHolder.l = library
			openImpl(library, containerLibraryElement)
		} else {
			eventBus.postVetoable(
				event = OpenLibraryRequest(library),
				undoEvent = OpenLibraryRequest(libraryHolder.library),
				thenHandler = {
					openImpl(library, containerLibraryElement)
				}
			)
		}
	}

	private fun openImpl(library: Library, containerLibraryElement: UUID? = null) {
		LOG.userTrail("Open main library '${library.name}' (${library.uuid})")
		libraryHolder.l = library
		if (containerLibraryElement != null) {
			library.getContainerLibraryElement(containerLibraryElement)?.let {
				eventBus.post(OpenContainerLibraryElementRequest(it))
			}
		}
	}

	/**
	 * Deletes the [Library] with the specified [UUID].
	 * @throws IllegalArgumentException if the [Library] is currently open
	 */
	fun delete(libraryId: LibraryIdentification) {
		if (libraryHolder.l?.uuid == libraryId.uuid) {
			throw IllegalArgumentException("illegal attempt to delete the currently open library ${libraryId.uuid}")
		}
		deleteImpl(libraryId)
	}

	fun canCopyContainerLibraryElement(element: ContainerLibraryElement, destination: Library): Boolean {
		libraryService.loadMetaGraph(element.library!!, element)
		return destination.containsAllRecursivelyReferencedBy(element.storable!!.graph.model!!)
	}

	/**
	 * Copies the specified [LibraryElement] from its [Library] to the destination [LibraryDirectory],
	 * which can also be part of another [Library].
	 */
	fun copyLibraryElement(element: LibraryElement, destination: LibraryDirectory) {
		LOG.trace("copy LibraryElement ${element.name}")
		when (element) {
			is BaseLibraryElement -> copyBaseElement(element, destination)
			is ContainerLibraryElement -> copyContainerLibraryElement(element, destination)
			else -> throw IllegalArgumentException("unsupported LibraryElement type ${element::class}")
		}
	}

	private fun copyBaseElement(element: BaseLibraryElement, destination: LibraryDirectory) {
		val clone = StorableCloner.clone(element)
		libraryService.addLibraryItem(destination.library!!, clone, destination, null)
	}

	private fun copyContainerLibraryElement(element: ContainerLibraryElement, destination: LibraryDirectory) {
		libraryService.loadMetaGraph(element.library!!, element)
		val clone = StorableCloner.clone(element.storable!!)
		libraryService.addContainerLibraryElement(destination.library!!, clone, destination, null)
	}
}