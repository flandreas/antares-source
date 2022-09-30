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

	fun result(param: String? = null, library: Library? = null): LibraryImportResult = LibraryImportResult(this, param, library)
}

data class LibraryImportResult(
	val type: LibraryImportResultType,
	val param: String? = null,
	val library: Library? = null)

/**
 * Base service class for managing [Libraries][Library] as well as [Projects][Project].
 *
 * @param userDictionaryService the service that allows changes of a [LibraryDictionary] by the user
 * @param systemDictionaryService the service for accessing the dictionary of system [Libraries][Library]
 */
abstract class AbstractLibraryManagementService(
	protected val libraryHolder: LibraryHolder,
	protected val libraryService: LibraryService,
	protected val userDictionaryService: LibraryDictionaryService,
	protected val systemDictionaryService: LibraryDictionaryService,
	protected val eventBus: EventBus
) {

	companion object {
		private val LOG by logger(AbstractLibraryManagementService::class)
	}

	/** Determines whether [name] already exists as the name of a stored [Library] in any language.*/
	abstract fun existsName(name: TranslatableText, except: UUID? = null): Boolean

	fun export(libraryId: LibraryIdentification, outputPath: String) {
		libraryService.exportLibrary(libraryId, outputPath)
	}

	fun import(inputPath: String, replaceIfUuidExists: Boolean): LibraryImportResult {
		lateinit var library: Library

		try {
			library = libraryService.importLibrary(inputPath, userDictionaryService.entriesCount, GraphQuota.UNLIMITED)
		} catch (e: LibraryImportConflictException) {
			if (replaceIfUuidExists) {
				deleteImpl(e.libraryId)
				try {
					library =
						libraryService.importLibrary(inputPath, userDictionaryService.entriesCount, GraphQuota.UNLIMITED)
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

		userDictionaryService.add(library)

		return Success.result(library = library)
	}

	protected fun deleteImpl(libraryId: LibraryIdentification) {
		libraryService.deleteLibrary(libraryId)
		userDictionaryService.remove(libraryId.uuid)
	}

	/** Checks whether [library] to be imported references another non-existing [Library]. */
	private fun hasStaleImportReferences(library: Library): Boolean =
		library.importedLibraryIds.any {
			!userDictionaryService.contains(it) && !systemDictionaryService.contains(it)
		}

	/** Closes the currently open [Library].*/
	fun close() {
		closeImpl { }
	}

	protected fun closeImpl(additionalThenHandler: () -> Unit) {
		if (libraryHolder.l != null) {
			val project = libraryHolder.library
			if (project != null) {
				eventBus.postVetoable(
					event = CloseLibraryRequest(),
					undoEvent = OpenLibraryRequest(project),
					thenHandler = {
						libraryHolder.l = null
						additionalThenHandler.invoke()
					}
				)
			}
		}
	}
}