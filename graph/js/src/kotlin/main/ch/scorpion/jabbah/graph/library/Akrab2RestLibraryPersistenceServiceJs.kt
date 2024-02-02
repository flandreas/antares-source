package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphBundle
import ch.scorpion.jabbah.graph.project.AkrabApiError
import ch.scorpion.jabbah.graph.project.AkrabApiException
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import org.w3c.xhr.XMLHttpRequest

class Akrab2RestLibraryPersistenceServiceJs(
    private val baseUrl: String
) : LibraryPersistenceService {

    companion object {
        private val LOG by logger(Akrab2RestLibraryPersistenceServiceJs::class)
    }

    override fun loadLibrary(libraryId: LibraryIdentification): Library {
        LOG.debug("GET project ${libraryId.uuid.id}")
        try {
            val request = XMLHttpRequest()
            request.open("GET", "$baseUrl/project/${libraryId.uuid.id}", async = false)
            request.overrideMimeType("text/xml")

            request.send()

            if (request.status != 200.toShort()) {
                LOG.error("Error in loadLibrary: status = ${request.status}")
                throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load Project: ${request.status}"))
            }

            return StoreXmlReader(DomXmlReader(request.responseXML!!)).readStorable() as Library
        } catch (e: Throwable) {
            LOG.error("Error in loadLibrary: ${e.message}")
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load Project: ${e.message}"))
        }
    }

    override fun loadMetaGraphXML(library: Library, uuid: UUID): String {
        LOG.debug("GET project MetaGraph $uuid")
        val request = XMLHttpRequest()
        request.open("GET", "$baseUrl/metaGraph/$uuid/xml", async = false)
        request.overrideMimeType("text/xml")

        request.send()

        if (request.status != 200.toShort()) {
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load project MetaGraph XML: ${request.status}"))
        }
        try {
            return request.responseText
        } catch (e: Throwable) {
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load project MetaGraph XML: ${e.message}"))
        }
    }

    override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
        try {
            return StoreXmlReader(DomXmlReader(loadMetaGraphXML(library, uuid))).readStorable() as MetaGraph
        } catch (e: Throwable) {
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load project MetaGraph: ${e.message}"))
        }
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