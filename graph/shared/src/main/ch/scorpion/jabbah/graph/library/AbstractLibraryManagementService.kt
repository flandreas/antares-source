package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.GraphQuotaException
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.library.LibraryImportResultType.*
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionary
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.model.element.ContainerLibraryElementCollector
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

	/**
	 * Removes the specified [Library] as import from the current [Library] in [LibraryHolder],
	 * as well as all [Libraries][Library] imported by [library].
	 *
	 * Clients like UI should first call [containsLibraryReference] to check whether removing
	 * the current [Library] contains a reference to one of the [MetaGraphs][MetaGraph] in the
	 * transitive hull of [library], and if that's the case, not allowing the user to remove it.
	 *
	 * @param library the [Library] not to be imported any more
	 */
	fun removeImport(library: Library) {
		LOG.userTrail("Remove import ${library.uuid} from ${libraryHolder.library.uuid}")
		libraryHolder.library.removeImport(library.uuid)
		libraryService.storeLibrary(libraryHolder.library)

		eventBus.post(LibraryImportsEvent(libraryHolder.library))
	}

	/**
	 * Determines whether [master] contains a [MetaGraph] with a reference to any [MetaGraph] in [target]
	 * (or any [Library] imported by [target]).
	 *
	 * This check can be costly, because every [MetaGraph] in the current [Library]has to be
	 * read and scanned for [SubGraphVerticeRefs][SubGraphVerticeRef] that would become
	 * broken when removing [target] from the imports.
	 */
	fun containsLibraryReference(master: Library, target: Library): Boolean {
		for (metaGraphId in master.metaGraphIds) {
			val metaGraph = master.getMetaGraph(metaGraphId)
			ContainerLibraryElementCollector()
				.collect(metaGraph.graph.model!!)
				.forEach { ref ->
					val elem = master.getContainerLibraryElement(ref)
					if (elem != null && target.expandedImports.libraries.map { it.uuid }.any { it == elem.library!!.uuid }) {
						return true
					}
				}

		}
		return false
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