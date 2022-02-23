package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.GraphQuotaException
import ch.scorpion.jabbah.graph.MetaGraphBundle

/**
 * Service methods for managing persistent library items and elements.
 */
interface LibraryPersistenceService {

	/** Loads the entire [MetaGraph] with the specified [UUID].*/
	fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph

	fun storeMetaGraph(library: Library, metaGraph: MetaGraph)

	fun deleteMetaGraph(library: Library, uuid: UUID)

	fun loadLibrary(libraryId: LibraryIdentification): Library

	fun storeLibrary(library: Library)

	fun deleteLibrary(libraryId: LibraryIdentification)

	/**
	 * Imports a [Library] contained in a ZIP file at the local `inputPath` and stores it as new [Library]
	 * with the [UUID] contained in the file.
	 * @return the [UUID] of the imported [Library]
	 * @throws IllegalArgumentException if the import file could not be read successfully
	 * @throws LibraryImportConflictException if a [Library] with the same [UUID] already exists
	 * @throws GraphQuotaException if the user's [GraphQuota] are not sufficient to import the [Library]
	 * @return the imported [Library]
	 * */
	fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota = GraphQuota.UNLIMITED): Library

	fun importTemporaryLibrary(libraryId: LibraryIdentification, temporaryPath: String)

	/** Exports the [Library] with the specified [UUID] into a ZIP file and stores it at `outputPath'. */
	fun exportLibrary(libraryId: LibraryIdentification, outputPath: String)

	/**
	 * Exports the [Library] with the specified [UUID]  and return the path of the temporary directory.
	 */
	fun exportLibraryTemporarily(libraryId: LibraryIdentification): String

	/**
	 * Exports [bundle] as a ZIP file to the specified location [outputPath].
	 */
	fun exportMetaGraphBundle(bundle: MetaGraphBundle, outputPath: String)

	/** Imports a [MetaGraphBundle] from a ZIP file at location [inputPath]. */
	fun importMetaGraphBundle(inputPath: String): MetaGraphBundle
}

/**
 * Thrown by [LibraryPersistenceService.importLibrary] if a [Library] with the same [UUID]
 * as the one to be imported already exists.
 * @property uuid the [UUID] of the [Library] to be imported
 */
data class LibraryImportConflictException(val libraryId: LibraryIdentification) : Throwable()

/** Null pattern.*/
class UnimplementedLibraryPersistenceService : LibraryPersistenceService {

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph =
		throw UnsupportedOperationException("not implemented")

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph): Unit =
		throw UnsupportedOperationException("not implemented")

	override fun deleteMetaGraph(library: Library, uuid: UUID): Unit =
		throw UnsupportedOperationException("not implemented")

	override fun loadLibrary(libraryId: LibraryIdentification): Library =
		throw UnsupportedOperationException("not implemented")

	override fun storeLibrary(library: Library): Unit =
		throw UnsupportedOperationException("not implemented")

	override fun deleteLibrary(libraryId: LibraryIdentification): Unit =
		throw UnsupportedOperationException("not implemented")

	override fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importTemporaryLibrary(libraryId: LibraryIdentification, temporaryPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportLibrary(libraryId: LibraryIdentification, outputPath: String): Unit =
		throw UnsupportedOperationException("not implemented")

	override fun exportLibraryTemporarily(libraryId: LibraryIdentification): String {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportMetaGraphBundle(bundle: MetaGraphBundle, outputPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importMetaGraphBundle(inputPath: String): MetaGraphBundle {
		throw UnsupportedOperationException("not implemented")
	}
}

class LibraryPersistenceServiceException(msg: String? = null) : Throwable(msg)