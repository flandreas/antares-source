package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.base.UUID

/**
 * Service methods for managing persistent library items and elements.
 */
interface LibraryService {

    fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph

    fun storeMetaGraph(library: Library, metaGraph: MetaGraph)

    fun deleteContainerLibraryElement(library: Library, uuid: UUID)

    fun loadLibrary(library: Library, fileName: String, locationPath: String? = null)

    fun storeLibrary(library: Library, fileName: String, locationPath: String? = null)

    fun exportLibrary(fileName: String, locationPath: String? = null)

}

/** Null pattern.*/
class UnimplementedLibraryService : LibraryService{
    override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
        throw UnsupportedOperationException("not implemented")
    }

    override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun deleteContainerLibraryElement(library: Library, uuid: UUID) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun loadLibrary(library: Library, fileName: String, locationPath: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun storeLibrary(library: Library, fileName: String, locationPath: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun exportLibrary(fileName: String, locationPath: String?) {
        throw UnsupportedOperationException("not implemented")
    }

}

class LibraryServiceException(msg: String? = null): Throwable(msg)