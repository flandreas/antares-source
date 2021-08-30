package ch.scorpion.jabbah.graph.library.dictionary

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import java.net.URL
import khttp.get
import khttp.put
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Calls the Graph REST API for working stored [LibraryDictionaries][LibraryDictionary].
 * @property projects `true` if this service accesses [Projects][Project],
 * `false` if it accesses [Libraries][Library]
 */
class RestLibraryDictionaryPersistenceService(
	baseUrl: URL,
	private val projects: Boolean
) : LibraryDictionaryPersistenceService {

	companion object {
		private val LOG by logger(RestLibraryDictionaryPersistenceService::class)
	}

	private val libraryDirectoryUrl: String = if (projects) {
		"$baseUrl/projects"
	} else {
		"$baseUrl/libraries"
	}

	/** ---- [LibraryDictionaryPersistenceService] interface */

	override val directoryExists: Boolean
		get() = TODO("not implemented")

	override fun load(): LibraryDictionary {
		val url = libraryDirectoryUrl
		LOG.trace("GET $url")
		val response = get(url)

		return StoreXmlReader(ElectricXmlReader(ByteArrayInputStream(response.text.toByteArray()))).readStorable()
	}

	override fun store(dictionary: LibraryDictionary) {
		val url = libraryDirectoryUrl
		val buffer = ByteArrayOutputStream()
		StoreXmlWriter(ElectricXmlWriter(buffer)).writeStorable(dictionary)
		val data = buffer.toString()

		put(url, data = ByteArrayInputStream(data.toByteArray()))
	}
}