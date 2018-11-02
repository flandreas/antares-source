package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.project.ProjectService
import org.apache.commons.io.FileUtils
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

/**
 * A [LibraryManagementService] that uses the local file system to store [Libraries][Library].
 */
class FileLibraryManagementService(
	override val defaultLibraryName: String,
	private val directoryPath: String,
	private val libraryFactory: LibraryFactory = LibraryModule.libraryFactory,
	private val libraryService: LibraryService = LibraryModule.libraryService.invoke(),
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	private val libraryDirectory: LibraryDictionary = LibraryModule.libraryDictionary,
	private val projectService: ProjectService = ProjectModule.projectService,
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
		return libraryDirectory.getLibraryNames()
	}

	override fun create(name: String): Library {
		if (exists(name)) {
			throw IllegalArgumentException("library name '$name' already exists")
		}
		LOG.debug("FileLibraryManagementService: creating new library '$name'")
		val library = libraryFactory.createBaseLibrary(name)
		libraryService.storeLibrary(library)
		eventBus.post(LibraryCreatedEvent(library))
		return library
	}

	override fun open(name: String): Library {
		val library = load(name)
		open(library)
		return library
	}

	override fun open(library: Library) {
		eventBus.postVetoable(
			event = OpenLibraryRequest(library),
			undoEvent = OpenLibraryRequest(libraryHolder.library),
			thenHandler = {
				libraryHolder.l = library
				projectService.close()
			}
		)
	}

	override fun delete(name: String) {
		throw UnsupportedOperationException("not implemented")
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
}