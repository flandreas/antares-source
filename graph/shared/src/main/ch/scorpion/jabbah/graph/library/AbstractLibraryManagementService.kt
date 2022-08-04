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
 * @param dictionaryService the service that allows changes of a [LibraryDictionary] by the user
 */
abstract class AbstractLibraryManagementService(
	protected val libraryHolder: LibraryHolder,
	protected val libraryService: LibraryService,
	protected val dictionaryService: LibraryDictionaryService,
	protected val eventBus: EventBus
) {

	companion object {
		private val LOG by logger(AbstractLibraryManagementService::class)
	}

	/** Determines whether [name] already exists as the name of a stored [Library] in any language.*/
	abstract fun existsName(name: TranslatableText, except: UUID? = null): Boolean

	/**
	 * Adds the specified [Library] as an imported [Library] to the [Library] currently held by
	 * [LibraryHolder] and makes this change persistent.
	 * @throws IllegalArgumentException if a [Library] with the specified [UUID] doesn't exist
	 * @throws IllegalStateException if [LibraryHolder] currently doesn't hold a library
	 */
	fun addImport(libraryId: UUID) {
		if (libraryHolder.l == null) {
			throw IllegalStateException("no Library to add an import to")
		}
		LOG.userTrail("Import library $libraryId in ${libraryHolder.library.uuid}")

		libraryHolder.library.addImport(libraryId)
		libraryService.storeLibrary(libraryHolder.library)

		eventBus.post(LibraryImportsEvent(libraryHolder.library))
	}

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

		return Success.result(library = library)
	}

	protected fun deleteImpl(libraryId: LibraryIdentification) {
		libraryService.deleteLibrary(libraryId)
		dictionaryService.remove(libraryId.uuid)
	}

	/** Checks whether [library] to be imported references another non-existing [Library]. */
	private fun hasStaleImportReferences(library: Library): Boolean {
		return library.importedLibraryIds.any {
			!dictionaryService.contains(it)
		}
	}
}