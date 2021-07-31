package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionary
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.project.Project

enum class LibraryImportResult {
	Success,
	NameAlreadyExists,
	Invalid,
	StaleLibraryReference,
	UuidAlreadyExists
}

/**
 * Base service class for managing [Libraries][Library] as well as [Projects][Project].
 *
 * @param dictionaryService the service that allows changes of a [LibraryDictionary] by the user
 */
abstract class AbstractLibraryManagementService(
	protected val libraryService: LibraryService,
	protected val dictionaryService: LibraryDictionaryService,
	protected val eventBus: EventBus
) {

	companion object {
		private val LOG by logger(AbstractLibraryManagementService::class)
	}

	/** Checks whether [library] to be imported references another non-existing [Library]. */
	abstract fun hasStaleImportReferences(library: Library): Boolean

	/** Determines whether [name] already exists as the name of a stored [Library] in any language.*/
	abstract fun existsName(name: TranslatableText, except: UUID? = null): Boolean

	fun export(uuid: UUID, outputPath: String) {
		libraryService.exportLibrary(uuid, outputPath)
	}

	fun import(inputPath: String, replaceIfUuidExists: Boolean): LibraryImportResult {
		lateinit var library: Library

		try {
			library = libraryService.importLibrary(inputPath)
		} catch (e: LibraryImportConflictException) {
			if (replaceIfUuidExists) {
				deleteImpl(e.uuid)
				try {
					library = libraryService.importLibrary(inputPath)
				} catch (e: Throwable) {
					return LibraryImportResult.Invalid
				}
			} else {
				return LibraryImportResult.UuidAlreadyExists
			}
		} catch (e: Throwable) {
			return LibraryImportResult.Invalid
		}

		if (existsName(library.name.translation)) {
			LOG.trace("Name of imported project already exists")
			libraryService.purgeLibrary(library.uuid)
			return LibraryImportResult.NameAlreadyExists
		}

		if (hasStaleImportReferences(library)) {
			LOG.trace("Library for imported project doesn't exist")
			libraryService.purgeLibrary(library.uuid)
			return LibraryImportResult.StaleLibraryReference
		}

		dictionaryService.add(library)

		return LibraryImportResult.Success
	}

	protected fun deleteImpl(uuid: UUID) {
		libraryService.deleteLibrary(uuid)
		dictionaryService.remove(uuid)
	}
}