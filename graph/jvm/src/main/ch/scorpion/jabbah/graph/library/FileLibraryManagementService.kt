package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCloner

/**
 * A [LibraryManagementService] that uses the local file system to store [Libraries][Library].
 */
class FileLibraryManagementService(
	private val libraryFactory: LibraryFactory = LibraryModule.libraryFactory,
	private val libraryService: LibraryService = LibraryModule.libraryService,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	private val dictionaryService: LibraryDictionaryService = LibraryModule.libraryDictionaryService,
	private val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
	private val eventBus: EventBus = BaseModule.eventBus
) : LibraryManagementService {

	companion object {
		private val LOG by logger(FileLibraryManagementService::class)
	}

	/** ---- [LibraryManagementService] interface */

	override fun exists(name: String): Boolean = dictionaryService.existsName(name)

	override fun getLibraryNames(): ImmutableList<String> = dictionaryService.getNames()

	override fun getLibraryDirectoryEntries(): ImmutableList<LibraryDictionaryEntry> = dictionaryService.getEntries()

	override fun create(properties: LibraryProperties, templateLibraryUuid: UUID?): Library {
		if (exists(properties.name)) {
			throw IllegalArgumentException("library name '${properties.name}' already exists")
		}
		LOG.debug("FileLibraryManagementService: creating new library '${properties.name}' with template $templateLibraryUuid")

		val library = if(templateLibraryUuid == null) {
			val library = libraryFactory.createEmptyLibrary(properties)
			libraryService.storeLibrary(library)
			library
		} else {
			libraryService.duplicateLibrary(loadLibrary(templateLibraryUuid), properties.name)
		}

		library.bindLibraryItems()
		dictionaryService.add(library)
		eventBus.post(LibraryCreatedEvent(library))
		return library
	}

	override fun update(properties: LibraryProperties) {
		val library = libraryHolder.library
		LOG.debug("FileLibraryManagementService: updating library ${library.uuid}")

		if (library.name != properties.name) {
			if (exists(properties.name)) {
				throw IllegalArgumentException("Library name '${properties.name}' already exists")
			}
			libraryService.renameLibrary(library, properties.name)
			dictionaryService.rename(library, properties.name)
		}

		library.properties = properties
		libraryService.storeLibrary(library)
		dictionaryService.update(library, properties)
		eventBus.post(LibraryPropertiesEvent(library, properties))
	}

	override fun loadLibrary(uuid: UUID): Library =
		libraryService.loadLibrary(uuid)

	override fun open(uuid: UUID): Library =
		libraryService.loadLibrary(uuid).also { open(it) }

	override fun open(library: Library) {
		LOG.debug("FileLibraryManagementService: open library ${library.uuid}")
		eventBus.postVetoable(
			event = OpenLibraryRequest(library),
			undoEvent = OpenLibraryRequest(libraryHolder.library),
			thenHandler = {
				libraryHolder.l = library
			}
		)
	}

	override fun delete(uuid: UUID) {
		if (libraryHolder.library.uuid == uuid) {
			throw IllegalArgumentException("illegal attempt to delete the currently open library $uuid")
		}
		libraryService.deleteLibrary(uuid)
		dictionaryService.remove(uuid)
	}

	override fun canCopyContainerLibraryElement(element: ContainerLibraryElement, destination: Library): Boolean {
		libraryService.loadMetaGraph(element.library!!, element)
		return destination.containsAllRecursivelyReferencedBy(element.metaGraph!!.graph.model!!)
	}

	override fun copyLibraryElement(element: LibraryElement, destination: LibraryDirectory) {
		LOG.debug("FileLibraryManagementService: copy LibraryElement ${element.name}")
		when (element) {
			is BaseLibraryElement -> copyBaseElement(element, destination)
			is ContainerLibraryElement -> copyContainerLibraryElement(element, destination)
			else -> throw IllegalArgumentException("unsupported LibraryElement type ${element::class}")
		}
	}

	/** ---- [FileLibraryManagementService] */

	private fun copyBaseElement(element: BaseLibraryElement, destination: LibraryDirectory) {
		val clone = storableCloner.clone(element) as BaseLibraryElement
		libraryService.addLibraryItem(destination.library!!, clone, destination, null)
	}

	private fun copyContainerLibraryElement(element: ContainerLibraryElement, destination: LibraryDirectory) {
		libraryService.loadMetaGraph(element.library!!, element)
		val clone = storableCloner.clone(element.metaGraph!!) as MetaGraph
		libraryService.addContainerLibraryElement(destination.library!!, clone, destination, null)
	}
}