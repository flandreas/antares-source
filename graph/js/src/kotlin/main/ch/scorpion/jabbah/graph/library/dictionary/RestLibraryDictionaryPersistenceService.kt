package ch.scorpion.jabbah.graph.library.dictionary

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import org.w3c.xhr.XMLHttpRequest

/**
 * An implementation of [LibraryDictionaryPersistenceService] that calls the REST services of [ch.scorpion.jabbah.graph]
 */
class RestLibraryDictionaryPersistenceService(
	private val baseUrl: String = BASE_URL,
	private val libraryDirectoryName: String,
	private val dictionaryFileName: String,
	directoryExists: Boolean
) : LibraryDictionaryPersistenceService {

	companion object {
		private val LOG by logger(RestLibraryDictionaryPersistenceService::class)
		private const val BASE_URL = ".."
	}

	override val directoryExists: Boolean = directoryExists

	override fun load(): LibraryDictionary {
		val request = XMLHttpRequest()
		val url = "$baseUrl/$libraryDirectoryName/$dictionaryFileName"
		LOG.trace("Calling GET $url")
		request.open("GET", url, async = false)
		request.overrideMimeType("text/xml")
		request.send()
		val doc = request.responseXML!!
		return StoreXmlReader(DomXmlReader(doc)).readStorable() as LibraryDictionary
	}

	override fun store(dictionary: LibraryDictionary) {
		throw UnsupportedOperationException("not implemented")
	}
}