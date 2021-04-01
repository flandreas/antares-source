package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.base.UUID

/**
 * Service methods for managing persistent library items and elements.
 */
interface LibraryPersistenceService {

	/** Loads the entire [MetaGraph] with the specified [UUID].*/
	fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph

	fun storeMetaGraph(library: Library, metaGraph: MetaGraph)

	fun deleteMetaGraph(library: Library, uuid: UUID)

	fun loadLibrary(uuid: UUID): Library

	fun storeLibrary(library: Library)

	fun deleteLibrary(uuid: UUID)

	/**
	 * Imports a [Library] contained in a ZIP file at `inputPath` and stores it as new [Library] with the [UUID] contained in the file.
	 * @return the [UUID] of the imported [Library]
	 * @throws IllegalStateException if a [Library] with the same [UUID] already exists
	 * */
	fun importLibrary(inputPath: String): UUID

	fun importTemporaryLibrary(uuid: UUID, temporaryPath: String)

	/** Exports the [Library] with the specified [UUID] into a ZIP file and stores it at `outputPath'. */
	fun exportLibrary(uuid: UUID, outputPath: String)

	/**
	 * Exports the [Library] with the specified [UUID]  and return the path of the temporary directory.
	 */
	fun exportLibraryTemporarily(uuid: UUID): String
}

/** Null pattern.*/
class UnimplementedLibraryPersistenceService : LibraryPersistenceService {

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph =
		throw UnsupportedOperationException("not implemented")

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph): Unit =
		throw UnsupportedOperationException("not implemented")

	override fun deleteMetaGraph(library: Library, uuid: UUID): Unit =
		throw UnsupportedOperationException("not implemented")

	override fun loadLibrary(uuid: UUID): Library =
		throw UnsupportedOperationException("not implemented")

	override fun storeLibrary(library: Library): Unit =
		throw UnsupportedOperationException("not implemented")

	override fun deleteLibrary(uuid: UUID): Unit =
		throw UnsupportedOperationException("not implemented")

	override fun importLibrary(inputPath: String):UUID {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importTemporaryLibrary(uuid: UUID, temporaryPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportLibrary(uuid: UUID, outputPath: String): Unit =
		throw UnsupportedOperationException("not implemented")

	override fun exportLibraryTemporarily(uuid: UUID): String {
		throw UnsupportedOperationException("not implemented")
	}
}

class LibraryPersistenceServiceException(msg: String? = null) : Throwable(msg)