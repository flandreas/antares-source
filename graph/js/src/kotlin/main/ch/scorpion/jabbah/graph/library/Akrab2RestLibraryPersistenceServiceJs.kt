package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphBundle

class Akrab2RestLibraryPersistenceServiceJs(
    private val baseUrl: String
) : LibraryPersistenceService {

    companion object {
        private val LOG by logger(Akrab2RestLibraryPersistenceServiceJs::class)
    }

    override fun loadLibrary(libraryId: LibraryIdentification): Library {
        LOG.debug("Loading library ${libraryId.uuid.id}")
        TODO("Not yet implemented")
    }

    override fun loadMetaGraphXML(library: Library, uuid: UUID): String {
        TODO("Not yet implemented")
    }

    override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
        TODO("Not yet implemented")
    }

    override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
        TODO("Not yet implemented")
    }

    override fun deleteMetaGraph(library: Library, uuid: UUID) {
        TODO("Not yet implemented")
    }

    override fun storeLibrary(library: Library) {
        TODO("Not yet implemented")
    }

    override fun deleteLibrary(libraryId: LibraryIdentification) {
        TODO("Not yet implemented")
    }

    override suspend fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library {
        TODO("Not yet implemented")
    }

    override fun importTemporaryLibrary(libraryId: LibraryIdentification, temporaryPath: String) {
        TODO("Not yet implemented")
    }

    override fun exportLibrary(libraryId: LibraryIdentification, outputPath: String) {
        TODO("Not yet implemented")
    }

    override fun exportLibraryTemporarily(libraryId: LibraryIdentification): String {
        TODO("Not yet implemented")
    }

    override fun exportMetaGraphBundle(bundle: MetaGraphBundle, outputPath: String) {
        TODO("Not yet implemented")
    }

    override fun importMetaGraphBundle(inputPath: String): MetaGraphBundle {
        TODO("Not yet implemented")
    }
}