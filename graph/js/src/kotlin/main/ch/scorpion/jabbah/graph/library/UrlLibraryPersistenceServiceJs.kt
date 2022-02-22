package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphBundle
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import org.w3c.xhr.XMLHttpRequest

/**
 * An implementation of [LibraryPersistenceService] that reads simple URL resources.
 * Mainly used for loading built-in system [Libraries][Library].
 */
class UrlLibraryPersistenceServiceJs(
	private val baseUrl: String = BASE_URL,
	private val libraryDirectoryName: String,
	private val libraryFileName: String
) : LibraryPersistenceService {

    companion object {
	    private val LOG by logger(UrlLibraryPersistenceServiceJs::class)
        private const val BASE_URL = ".."
    }

    /** ---- [LibraryPersistenceService] */

    override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
        val request = XMLHttpRequest()
	    val url = "$baseUrl/$libraryDirectoryName/${library.uuid.id}/${uuid.id}.cir"
	    LOG.trace("Calling GET $url")
        request.open("GET", url, async = false)
        request.overrideMimeType("text/xml")
        request.send()
	    val doc = request.responseXML!!
        return StoreXmlReader(DomXmlReader(doc)).readStorable() as MetaGraph
    }

	private fun buildLibraryFilePath(libraryUuid: UUID): String =
		"${buildLibraryDirectoryPath(libraryUuid)}/$libraryFileName"

	private fun buildLibraryDirectoryPath(libraryUuid: UUID): String =
		"$baseUrl/$libraryDirectoryName/${libraryUuid}"

    override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun deleteMetaGraph(library: Library, uuid: UUID) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun loadLibrary(uuid: UUID): Library {
	    val request = XMLHttpRequest()
	    request.open("GET", buildLibraryFilePath(uuid), async = false)
	    request.overrideMimeType("text/xml")
	    request.send()
	    return StoreXmlReader(DomXmlReader(request.responseXML!!)).readStorable() as Library
    }

    override fun storeLibrary(library: Library) {
        throw UnsupportedOperationException("not implemented")
    }

	override fun deleteLibrary(uuid: UUID) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importTemporaryLibrary(uuid: UUID, temporaryPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportLibrary(uuid: UUID, outputPath: String) {
        throw UnsupportedOperationException("not implemented")
    }

	override fun exportLibraryTemporarily(uuid: UUID): String {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportMetaGraphBundle(bundle: MetaGraphBundle, outputPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importMetaGraphBundle(inputPath: String): MetaGraphBundle {
		throw UnsupportedOperationException("not implemented")
	}
}