package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.base.UUID

/**
 * Service methods for managing persistent library items and elements.
 */
interface LibraryPersistenceService {

    fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph

    fun storeMetaGraph(library: Library, metaGraph: MetaGraph)

    fun deleteContainerLibraryElement(library: Library, uuid: UUID)

	fun loadLibrary(name: String): Library

    fun storeLibrary(library: Library)

    fun exportLibrary(fileName: String, locationPath: String? = null)

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

    override fun exportLibrary(fileName: String, locationPath: String?) {
        throw UnsupportedOperationException("not implemented")
    }

}

class LibraryPersistenceServiceException(msg: String? = null): Throwable(msg)