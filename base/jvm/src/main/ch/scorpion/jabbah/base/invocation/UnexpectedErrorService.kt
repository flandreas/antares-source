package ch.scorpion.jabbah.base.invocation

import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.base.UserActionTrail
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.net.httpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.apache.commons.io.output.StringBuilderWriter
import java.net.URL

interface UnexpectedErrorService {

    fun buildDescription(versionId: String, stackTrace: String): String

    suspend fun sendUnexpectedError(description: String)
}

class UnexpectedErrorServiceImpl(
    private val baseUrl: URL
) : UnexpectedErrorService {

    companion object {
        private val LOG by logger(UnexpectedErrorServiceImpl::class)
        private const val API_KEY = "heyAPI"
    }

    override fun buildDescription(versionId: String, stackTrace: String): String {
        val writer = StringBuilderWriter()
        if (versionId.isNotBlank()) {
            writer.appendLine("Version: $versionId")
        }
        writer.append(UserActionTrail.toString())
        writer.append(stackTrace)
        return writer.toString()
    }

    override suspend fun sendUnexpectedError(description: String) {
        val url = "$baseUrl/system/error"
        LOG.info("Sending unexpected error report to $url")
        val request = UnexpectedErrorRequest(API_KEY, description)

        val response: HttpResponse = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        LOG.info("=> Response: ${response.status}")
    }
}

@Serializable
data class UnexpectedErrorRequest(
    val apiKey: String,
    val description: String
) : Bean