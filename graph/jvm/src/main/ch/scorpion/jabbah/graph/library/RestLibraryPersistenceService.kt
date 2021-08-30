package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphBundle
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import khttp.get
import khttp.post
import khttp.put
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL

/**
 * Calls the Graph REST API for working with stored [Libraries][Library].
 * @property projects `true` if this service accesses [Projects][Project],
 * `false` if it accesses [Libraries][Library]
 */
class RestLibraryPersistenceService(
	baseUrl: URL,
	private val projects: Boolean
) : LibraryPersistenceService {

	companion object {
		private val LOG by logger(RestLibraryPersistenceService::class)
	}

	private val libraryDirectoryUrl: String = if (projects) {
		"$baseUrl/projects"
	} else {
		"$baseUrl/libraries"
	}

	/** ---- [LibraryPersistenceService] */

	override fun loadLibrary(uuid: UUID): Library {
		val url = "${buildLibraryFilePath(uuid)}"
		LOG.trace("GET $url")
		val response = get(url)

		return StoreXmlReader(ElectricXmlReader(ByteArrayInputStream(response.text.toByteArray()))).readStorable()
	}

	override fun storeLibrary(library: Library) {
		val url = "${buildLibraryFilePath(library.uuid)}"
		LOG.trace("PUT $url")
		val buffer = ByteArrayOutputStream()
		StoreXmlWriter(ElectricXmlWriter(buffer)).writeStorable(library)
		val data = buffer.toString()

		put(url, data = ByteArrayInputStream(data.toByteArray()))
	}

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
		val url = "${buildLibraryFilePath(library.uuid)}/$uuid"
		LOG.trace("GET $url")
		val response = get(url)

		return StoreXmlReader(ElectricXmlReader(ByteArrayInputStream(response.text.toByteArray()))).readStorable()
	}

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
		val url = "${buildLibraryFilePath(library.uuid)}/${metaGraph.uuid}"
		LOG.trace("POST $url")
		val buffer = ByteArrayOutputStream()
		StoreXmlWriter(ElectricXmlWriter(buffer)).writeStorable(metaGraph)
		val data = buffer.toString()

		post(url, data = ByteArrayInputStream(data.toByteArray()))
	}

	override fun deleteMetaGraph(library: Library, uuid: UUID) {
		throw UnsupportedOperationException("not implemented")
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

	private fun buildLibraryFilePath(libraryUuid: UUID): String =
		"$libraryDirectoryUrl/$libraryUuid"
}