package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphBundle
import ch.scorpion.jabbah.graph.project.AkrabApiError
import ch.scorpion.jabbah.graph.project.AkrabApiException
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StorableCloner
import ch.scorpion.jabbah.io.StoreXmlReader
import org.w3c.xhr.XMLHttpRequest

/**
 * An implementation of [LibraryPersistenceService] that calls the REST endpoints of Akrab.
 * Performs synchronous (blocking) calls.
 * TODO: Consider using Kotlin Coroutines.
 * TODO: Delete, replaced by Akrab2RestLibraryPersistenceServiceJs
 */
class AkrabRestLibraryPersistenceServiceJs(
	private val baseUrl: String,
	private val dictionaryName: String
) : LibraryPersistenceService {

	companion object {
		private val LOG by logger(AkrabRestLibraryPersistenceServiceJs::class)
	}

	/**
	 * The optional access token used for REST API authentication.
	 * Can be `null` (anonymous) when using the public REST API.
	 */
	var accessToken: String? = null

	/** ---- [LibraryPersistenceService] interface. */

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
		try {
			return StoreXmlReader(DomXmlReader(loadMetaGraphXML(library, uuid))).readStorable() as MetaGraph
		} catch (e: Throwable) {
			throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load graph: ${e.message}"))
		}
	}

	override fun loadMetaGraphXML(library: Library, uuid: UUID): String {
		val path = buildMetaGraphPath(library.uuid, uuid)
		LOG.debug("Loading MetaGraph with GET $path")

		val request = XMLHttpRequest()
		request.open("GET", path, async = false)
		accessToken?.let {
			request.setRequestHeader("Authorization", "Bearer $it")
		}
		request.overrideMimeType("text/xml")
		request.send()
		if (request.status != 200.toShort()) {
			throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load graph: ${request.status}"))
		}

		try {
			return request.responseText
		} catch (e: Throwable) {
			throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load graph: ${e.message}"))
		}
	}

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
		val path = buildMetaGraphPath(library.uuid, metaGraph.uuid)
		LOG.debug("Storing MetaGraph with PUT $path")

		val request = XMLHttpRequest()
		request.open("PUT", path, async = false)
		accessToken?.let {
			request.setRequestHeader("Authorization", "Bearer $accessToken")
		}
		request.overrideMimeType("text/xml")
		request.send(
			StorableCloner.serialize(metaGraph)
		)
	}

	override fun deleteMetaGraph(library: Library, uuid: UUID) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun loadLibrary(libraryId: LibraryIdentification): Library {
		val path = buildLibraryDictionaryPath(libraryId.uuid)
		LOG.debug("Loading library with GET $path")

		val request = XMLHttpRequest()
		request.open("GET", path, async = false)
		accessToken?.let {
			request.setRequestHeader("Authorization", "Bearer $accessToken")
		}
		request.overrideMimeType("text/xml")
		request.send()

		if (request.status != 200.toShort()) {
			throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load library: ${request.status}"))
		}
		try {
			return StoreXmlReader(DomXmlReader(request.responseXML!!)).readStorable() as Library
		} catch (e: Throwable) {
			throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load library: ${e.message}"))
		}
	}

	override fun storeLibrary(library: Library) {
		throw UnsupportedOperationException("storeLibrary not implemented")
	}

	override fun deleteLibrary(libraryId: LibraryIdentification) {
		throw UnsupportedOperationException("not implemented")
	}

	override suspend fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importTemporaryLibrary(libraryId: LibraryIdentification, temporaryPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportLibrary(libraryId: LibraryIdentification, outputPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportLibraryTemporarily(libraryId: LibraryIdentification): String {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportMetaGraphBundle(bundle: MetaGraphBundle, outputPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importMetaGraphBundle(inputPath: String): MetaGraphBundle {
		throw UnsupportedOperationException("not implemented")
	}

	/** ---- [AkrabRestLibraryPersistenceServiceJs] */

	private fun buildLibraryDictionaryPath(libraryId: UUID): String =
		"${buildLibraryBasePath(libraryId)}/$dictionaryName"

	private fun buildLibraryBasePath(libraryId: UUID): String =
		"$baseUrl/${libraryId.id}"

	private fun buildMetaGraphPath(libraryId: UUID, metaGraphUuid: UUID): String =
		"${buildLibraryBasePath(libraryId)}/$dictionaryName/${metaGraphUuid.id}"
}