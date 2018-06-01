package ch.scoprion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryFolder
import ch.scorpion.jabbah.graph.library.LibraryPersistenceService
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import org.w3c.dom.Document
import org.w3c.xhr.XMLHttpRequest

/**
 * An implementation of [LibraryPersistenceService] that calls the REST services of [ch.scorpion.jabbah.graph].
 */
class RestLibraryPersistenceService : LibraryPersistenceService {

    companion object {
        private val BASE_URL = "http://localhost:4567/jabbah-graph"
    }

    /** ---- [LibraryPersistenceService] */

    override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
        val request = XMLHttpRequest()
        request.open("GET", "$BASE_URL/library/graphView/${uuid.id}", async = false)
        //request.responseType = XMLHttpRequestResponseType.DOCUMENT
        request.overrideMimeType("text/xml")
//        request.onload = {
//            handleLibraryResponse(library, request.responseXML!!)
//        }
        request.send()
        return StoreXmlReader(DomXmlReader(request.responseXML!!)).readStorable() as MetaGraph
    }

    override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun deleteContainerLibraryElement(library: Library, uuid: UUID) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun loadLibrary(library: Library) {
        val request = XMLHttpRequest()
        request.open("GET", "$BASE_URL/library/contents", async = false)
        //request.responseType = XMLHttpRequestResponseType.DOCUMENT
        request.overrideMimeType("text/xml")
        request.onload = {
            handleLibraryResponse(library, request.responseXML!!)
        }
        request.send()
    }

    override fun storeLibrary(library: Library) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun exportLibrary(fileName: String, locationPath: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    /** ---- [RestLibraryPersistenceService] */

    private fun handleLibraryResponse(library: Library, doc: Document) {
        val libraryFolder = StoreXmlReader(DomXmlReader(doc)).readStorable() as LibraryFolder
        library.replaceContentsWith(libraryFolder)
    }
}