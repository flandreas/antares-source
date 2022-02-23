package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.GraphQuotaException
import ch.scorpion.jabbah.graph.library.LibraryImportResultType.*
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionary
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.project.Project

enum class LibraryImportResultType {
	Success,
	NameAlreadyExists,
	Invalid,
	StaleLibraryReference,
	UuidAlreadyExists,
	QuotaExceeded;

	fun result(param: String? = null): LibraryImportResult = LibraryImportResult(this, param)
}

data class LibraryImportResult(
	val type: LibraryImportResultType, val param: String? = null)

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

	fun export(libraryId: LibraryIdentification, outputPath: String) {
		libraryService.exportLibrary(libraryId, outputPath)
	}

	fun import(inputPath: String, replaceIfUuidExists: Boolean): LibraryImportResult {
		lateinit var library: Library

		try {
			library = libraryService.importLibrary(inputPath, dictionaryService.entriesCount, GraphQuota.UNLIMITED)
		} catch (e: LibraryImportConflictException) {
			if (replaceIfUuidExists) {
				deleteImpl(e.libraryId)
				try {
					library =
						libraryService.importLibrary(inputPath, dictionaryService.entriesCount, GraphQuota.UNLIMITED)
				} catch (e: Throwable) {
					return Invalid.result()
				}
			} else {
				return UuidAlreadyExists.result()
			}
		} catch (e: GraphQuotaException) {
			return QuotaExceeded.result(e.translatedMsg)
		} catch (e: Throwable) {
			return Invalid.result()
		}

		if (existsName(library.name.translation)) {
			LOG.trace("Name of imported project already exists")
			libraryService.purgeLibrary(library.identification)
			return NameAlreadyExists.result()
		}

		if (hasStaleImportReferences(library)) {
			LOG.trace("Library for imported project doesn't exist")
			libraryService.purgeLibrary(library.identification)
			return StaleLibraryReference.result()
		}

		dictionaryService.add(library)

		return Success.result()
	}

	protected fun deleteImpl(libraryId: LibraryIdentification) {
		libraryService.deleteLibrary(libraryId)
		dictionaryService.remove(libraryId.uuid)
	}
}