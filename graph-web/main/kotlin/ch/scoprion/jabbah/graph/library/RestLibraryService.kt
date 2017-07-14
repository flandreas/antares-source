package ch.scoprion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryFolder
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import org.w3c.dom.Document
import org.w3c.xhr.DOCUMENT
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType

/**
 * An implementation of [LibraryService] that calls the REST services of [ch.scorpion.jabbah.graph].
 */
class RestLibraryService : LibraryService {

    companion object {
        private val BASE_URL = "http://localhost:4567/jabbah-graph"
    }

    /** ---- [LibraryService] */

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

    override fun loadLibrary(library: Library, fileName: String, locationPath: String?) {
        val request = XMLHttpRequest()
        request.open("GET", "$BASE_URL/library/contents", async = false)
        //request.responseType = XMLHttpRequestResponseType.DOCUMENT
        request.overrideMimeType("text/xml")
        request.onload = {
            handleLibraryResponse(library, request.responseXML!!)
        }
        request.send()
    }

    override fun storeLibrary(library: Library, fileName: String, locationPath: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    /** ---- [RestLibraryService] */

    private fun handleLibraryResponse(library: Library, doc: Document) {
        val libraryFolder = StoreXmlReader(DomXmlReader(doc)).readStorable() as LibraryFolder
        library.replaceContentsWith(libraryFolder)
    }
}