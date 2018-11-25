package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCloner
import org.apache.commons.io.FileUtils
import java.nio.file.FileSystems
import java.nio.file.Files

/**
 * A [LibraryManagementService] that uses the local file system to store [Libraries][Library].
 */
class FileLibraryManagementService(
	override val defaultLibraryName: String,
	private val directoryPath: String,
	private val libraryFactory: LibraryFactory = LibraryModule.libraryFactory,
	private val libraryService: LibraryService = LibraryModule.libraryService.invoke(),
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	private val libraryDictionary: LibraryDictionary = LibraryModule.libraryDictionary,
	private val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
	private val eventBus: EventBus = BaseModule.eventBus
) : LibraryManagementService {

	companion object {
		private val LOG by logger(FileLibraryManagementService::class)
	}

	/** ---- [LibraryManagementService] interface */

	override val currentLibrary: Library get() = libraryHolder.library

	override fun exists(name: String): Boolean {
		return Files.exists(FileSystems.getDefault().getPath(directoryPath, name))
	}

	override fun getLibraryNames(): ImmutableList<String> {
		return libraryDictionary.getLibraryNames()
	}

	override fun getLibraryDirectoryEntries(): ImmutableList<LibraryDictionaryEntry> {
		return libraryDictionary.getEntries()
	}

	override fun create(properties: LibraryProperties, templateLibraryName: String?): Library {
		if (exists(properties.name)) {
			throw IllegalArgumentException("library name '${properties.name}' already exists")
		}
		LOG.debug("FileLibraryManagementService: creating new library '${properties.name}' with template $templateLibraryName")

		val library = if(StringUtils.isBlank(templateLibraryName)) {
			val library = libraryFactory.createEmptyLibrary(properties)
			libraryService.storeLibrary(library)
			library
		} else {
			libraryService.duplicateLibrary(loadLibrary(templateLibraryName!!), properties.name)
		}

		library.bindLibraryItems()
		eventBus.post(LibraryCreatedEvent(library))
		return library
	}

	override fun update(properties: LibraryProperties) {
		if (exists(properties.name)) {
			throw IllegalArgumentException("Library name '${properties.name}' already exists")
		}
		val library = libraryHolder.library
		LOG.debug("FileLibraryManagementService: updating library '${library.name}'")

		if (library.name != properties.name) {
			libraryService.renameLibrary(library, properties.name)
		}

		library.properties = properties
		libraryService.storeLibrary(library)
		eventBus.post(LibraryPropertiesEvent(library, properties))
	}

	override fun loadLibrary(name: String): Library {
		return libraryService.loadLibrary(name)
	}

	override fun open(name: String): Library {
		val library = load(name)
		open(library)
		return library
	}

	override fun open(uuid: UUID): Library {
		val library = load(libraryDictionary.getNameOfUUID(uuid))
		open(library)
		return library
	}

	override fun open(library: Library) {
		LOG.debug("FileLibraryManagementService: open library '${library.name}'")
		eventBus.postVetoable(
			event = OpenLibraryRequest(library),
			undoEvent = OpenLibraryRequest(libraryHolder.library),
			thenHandler = {
				libraryHolder.l = library
			}
		)
	}

	override fun delete(name: String) {
		if (libraryHolder.library.name == name) {
			throw IllegalArgumentException("illegal attempt to delete the currently open library '$name'")
		}
		libraryService.deleteLibrary(name)
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

	private fun load(name: String): Library {
		if (!exists(name)) {
			throw IllegalArgumentException("library '$name' doesn't exist")
		}
		return libraryService.loadLibrary(name)
	}

	private fun deleteImpl(libraryName: String) {
		FileUtils.forceDelete(FileSystems.getDefault().getPath(directoryPath, libraryName).toFile())
	}

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