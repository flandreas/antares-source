package ch.scorpion.jabbah.base.invocation

import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.base.UserActionTrail
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.net.httpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.apache.commons.io.output.StringBuilderWriter
import java.io.File
import java.net.URL
import java.time.Duration
import java.time.LocalDateTime

interface UnexpectedErrorService {

    fun buildDescription(versionId: String, stackTrace: String): String

    suspend fun sendUnexpectedError(description: String)

    suspend fun sendErrorDump(path: String): Boolean
}

class UnexpectedErrorServiceImpl(
    var baseUrl: URL
) : UnexpectedErrorService {

    companion object {
        private val LOG by logger(UnexpectedErrorServiceImpl::class)

        private const val API_KEY = "heyAPI"

        /** The [Duration] since the last unexpected error in which new errors are ignored (without restart). */
        private val MIN_DURATION = Duration.ofHours(12)
    }

    /** The time when the last unexpected error occurred. Used to avoid uploading the same error multiple times. */
    private var lastDateTime: LocalDateTime? = null

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
        if (lastDateTime != null && Duration.between(lastDateTime, LocalDateTime.now()) < MIN_DURATION) {
            return
        }

        val url = "$baseUrl/system/error"
        LOG.info("Sending unexpected error report to $url")
        val request = UnexpectedErrorRequest(API_KEY, description)

        val response: HttpResponse = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        LOG.info("=> Response: ${response.status}")

        lastDateTime = LocalDateTime.now()
    }

    override suspend fun sendErrorDump(path: String): Boolean {
        try {
            val url = "$baseUrl/system/errorDump"
            LOG.info("Uploading error dump to $url")
            val file = File(path)

            val response = httpClient.post(url) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("apiKey", API_KEY)
                            append("dump", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "application/zip")
                                append(HttpHeaders.ContentDisposition, "filename=\"dump.zip\"")
                            })
                        }
                    )
                )
            }

            LOG.info("Upload: Status = ${response.status}")
            return true
        } catch (e: Exception) {
            LOG.error("Error while uploading error dump", e)
            return false
        }
    }
}

@Serializable
data class UnexpectedErrorRequest(
    val apiKey: String,
    val description: String
) : Bean