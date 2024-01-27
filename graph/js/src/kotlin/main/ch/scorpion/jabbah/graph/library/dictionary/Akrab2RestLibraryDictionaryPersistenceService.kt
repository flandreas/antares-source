package ch.scorpion.jabbah.graph.library.dictionary

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import org.w3c.xhr.XMLHttpRequest

class Akrab2RestLibraryDictionaryPersistenceService(
    private val baseUrl: String
) : LibraryDictionaryPersistenceService {

    companion object {
        private val LOG by logger(Akrab2RestLibraryDictionaryPersistenceService::class)
    }

    override val directoryExists: Boolean = true

    override fun ensureLibraryDirectory() { }

    override fun store(dictionary: LibraryDictionary) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun load(): LibraryDictionary {
        val request = XMLHttpRequest()
        val url = "$baseUrl/library/system"
        LOG.trace("Calling GET $url")
        request.open("GET", url, async = false)
        request.overrideMimeType("text/xml")
        request.send()
        val doc = request.responseXML!!
        return StoreXmlReader(DomXmlReader(doc)).readStorable() as LibraryDictionary
    }
}