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

    fun deleteContainerLibraryElement(library: Library, uuid: UUID)

	fun loadLibrary(name: String): Library

    fun storeLibrary(library: Library)

	fun deleteLibrary(name: String)

	/** Duplicates the specified [Library] and stores the duplicate with the given new name.*/
	fun duplicateLibrary(library: Library, newName: String)

	/** Stores the renaming of a [Library].*/
	fun renameLibrary(library: Library, newName: String)

	/** Imports a [Library] contained in a ZIP file at `inputPath` and stores it as new [Library] with the given name.*/
	fun importLibrary(name: String, inputPath: String)

	/** Exports the [Library] with the specified name into a ZIP file and stores it at `outputPath'. */
    fun exportLibrary(name: String, outputPath: String)
}

/** Null pattern.*/
class UnimplementedLibraryPersistenceService : LibraryPersistenceService{

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
        throw UnsupportedOperationException("not implemented")
    }

    override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun deleteContainerLibraryElement(library: Library, uuid: UUID) {
        throw UnsupportedOperationException("not implemented")
    }

	override fun loadLibrary(name: String): Library {
		throw UnsupportedOperationException("not implemented")
	}

	override fun storeLibrary(library: Library) {
        throw UnsupportedOperationException("not implemented")
    }

	override fun deleteLibrary(name: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun duplicateLibrary(library: Library, newName: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun renameLibrary(library: Library, newName: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importLibrary(name: String, inputPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

    override fun exportLibrary(name: String, outputPath: String) {
        throw UnsupportedOperationException("not implemented")
    }

}

class LibraryPersistenceServiceException(msg: String? = null): Throwable(msg)