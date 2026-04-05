package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.project.AkrabApiError
import io.antarescircuit.jabbah.graph.project.AkrabApiException
import io.antarescircuit.jabbah.io.DomXmlReader
import io.antarescircuit.jabbah.io.StoreXmlReader
import kotlinx.browser.window
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Promise

abstract class AbstractAkrab2RestLibraryPersistenceServiceJs(
    protected val baseUrl: String,
    private val logQualifier: String
) : LibraryPersistenceService {

    companion object {
        private val LOG by logger(AbstractAkrab2RestLibraryPersistenceServiceJs::class)
    }

    protected open fun loadLibrary(libraryId: LibraryIdentification, url: String): Library {
        LOG.debug("GET $logQualifier ${libraryId.uuid.id}")
        try {
            val request = XMLHttpRequest()
            request.open("GET", url, async = false)
            request.overrideMimeType("text/xml")

            request.send()

            if (request.status != 200.toShort()) {
                LOG.error("Error in loadLibrary: status = ${request.status}")
                throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load $logQualifier: ${request.status}"))
            }

            return StoreXmlReader(DomXmlReader(request.responseXML!!)).readStorable() as Library
        } catch (e: Throwable) {
            LOG.error("Error in loadLibrary: ${e.message}")
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load $logQualifier: ${e.message}"))
        }
    }

    protected abstract fun getMetaGraphXMLUrl(library: Library, uuid: UUID): String

    override fun loadMetaGraphXML(library: Library, uuid: UUID): String {
        val url = getMetaGraphXMLUrl(library, uuid)
        LOG.debug("GET system $logQualifier MetaGraph $uuid")
        val request = XMLHttpRequest()
        request.open("GET", url, async = false)
        request.overrideMimeType("text/xml")

        request.send()

        if (request.status != 200.toShort()) {
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load $logQualifier MetaGraph XML: ${request.status}"))
        }
        try {
            return request.responseText
        } catch (e: Throwable) {
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load $logQualifier MetaGraph XML: ${e.message}"))
        }
    }

    override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
        try {
            return StoreXmlReader(DomXmlReader(loadMetaGraphXML(library, uuid))).readStorable() as MetaGraph
        } catch (e: Throwable) {
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load $logQualifier MetaGraph: ${e.message}"))
        }
    }

    fun loadMetaGraphAsync(library: Library, uuid: UUID): Promise<MetaGraph> {
        val url = getMetaGraphXMLUrl(library, uuid)
        val headers = Headers()
        headers.append("Content-Type", "text/xml")

        var status = 200
        return window.fetch(url, RequestInit("GET", headers))
            .then {
                status = it.status.toInt()
                it.text()
            }
            .then {
                if (status != 200) {
                    throw Error(it)
                }
                try {
                    StoreXmlReader(DomXmlReader(it)).readStorable() as MetaGraph
                } catch (e: Throwable) {
                    println("Hallo Sepp")
                    LOG.error("Error in loadMetaGraphAsync: ${e.message}")
                    e.printStackTrace()
                    throw e
                }
            }
    }
}