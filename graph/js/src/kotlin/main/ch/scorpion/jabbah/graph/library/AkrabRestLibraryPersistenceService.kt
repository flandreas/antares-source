package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphBundle
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StorableCloner
import ch.scorpion.jabbah.io.StoreXmlReader
import org.w3c.xhr.XMLHttpRequest

/**
 * An implementation of [LibraryPersistenceService] that calls the REST endpoints of Akrab.
 * Performs synchronous (blocking) calls.
 * TODO: Consider using Kotlin Coroutines.
 * TODO: Add authorization
 */
class AkrabRestLibraryPersistenceService(
	private val baseUrl: String,
	private val dictionaryName: String
) : LibraryPersistenceService {

	/** ---- [LibraryPersistenceService] interface. */

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
		val request = XMLHttpRequest()
		request.open("GET", buildMetaGraphPath(library.uuid, uuid), async = false)
		request.overrideMimeType("text/xml")
		request.send()
		return StoreXmlReader(DomXmlReader(request.responseXML!!)).readStorable() as MetaGraph
	}

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
		val request = XMLHttpRequest()
		request.open("PUT", buildMetaGraphPath(library.uuid, metaGraph.uuid), async = false)
		request.overrideMimeType("text/xml")
		request.send(
			StorableCloner.serialize(metaGraph)
		)
	}

	override fun deleteMetaGraph(library: Library, uuid: UUID) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun loadLibrary(uuid: UUID): Library {
		val request = XMLHttpRequest()
		request.open("GET", buildLibraryDictionaryPath(uuid), async = false)
		request.overrideMimeType("text/xml")
		request.send()
		return StoreXmlReader(DomXmlReader(request.responseXML!!)).readStorable() as Library
	}

	override fun storeLibrary(library: Library) {
		throw UnsupportedOperationException("storeLibrary not implemented")
	}

	override fun deleteLibrary(uuid: UUID) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importLibrary(inputPath: String): UUID {
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

	/** ---- [AkrabRestLibraryPersistenceService] */

	private fun buildLibraryDictionaryPath(libraryId: UUID): String =
		"${buildLibraryBasePath(libraryId)}/$dictionaryName"

	private fun buildLibraryBasePath(libraryId: UUID): String =
		"$baseUrl/${libraryId.id}"

	private fun buildMetaGraphPath(libraryId: UUID, metaGraphUuid: UUID): String =
		"${buildLibraryBasePath(libraryId)}/$dictionaryName/${metaGraphUuid.id}"
}