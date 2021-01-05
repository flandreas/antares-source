package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import org.w3c.dom.Document
import org.w3c.xhr.XMLHttpRequest

/**
 * An implementation of [LibraryPersistenceService] that calls the REST services of [ch.scorpion.jabbah.graph].
 */
class RestLibraryPersistenceService(
	private val baseUrl: String = BASE_URL
) : LibraryPersistenceService {

    companion object {
        private const val BASE_URL = "http://localhost:4567/jabbah-graph"
    }

    /** ---- [LibraryPersistenceService] */

    override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
        val request = XMLHttpRequest()
	    val url = "$baseUrl/libraries/${library.uuid.id}/${uuid.id}.cir"
        request.open("GET", url, async = false)
        request.overrideMimeType("text/xml")
        request.send()
        return StoreXmlReader(DomXmlReader(request.responseXML!!)).readStorable() as MetaGraph
    }

    override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun deleteMetaGraph(library: Library, uuid: UUID) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun loadLibrary(uuid: UUID): Library {
	    throw UnsupportedOperationException("not implemented")
	    /*
        val request = XMLHttpRequest()
        request.open("GET", "$BASE_URL/library/contents", async = false)
        //request.responseType = XMLHttpRequestResponseType.DOCUMENT
        request.overrideMimeType("text/xml")
        request.onload = {
            handleLibraryResponse(library, request.responseXML!!)
        }
        request.send()
	    */
    }

    override fun storeLibrary(library: Library) {
        throw UnsupportedOperationException("not implemented")
    }

	override fun deleteLibrary(uuid: UUID) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun duplicateLibrary(library: Library, newUuid: UUID) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importLibrary(inputPath: String): UUID {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportLibrary(uuid: UUID, outputPath: String) {
        throw UnsupportedOperationException("not implemented")
    }

    /** ---- [RestLibraryPersistenceService] */

    private fun handleLibraryResponse(library: Library, doc: Document) {
        val libraryFolder = StoreXmlReader(DomXmlReader(doc)).readStorable() as LibraryFolder
        library.replaceContentsWith(libraryFolder)
    }
}