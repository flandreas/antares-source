package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.net.httpClient
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.login.Session
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import kotlin.io.path.absolutePathString

/**
 * A client-side (JVM) service for calling Akrab REST endpoints of [Project].
 */
class ProjectAkrabClientServiceJvm(
	private val baseUrl: URL,
	private val persistenceService: FileLibraryPersistenceService
) {
	companion object {
		private val LOG by logger(ProjectAkrabClientServiceJvm::class)
	}

	/**
	 * Creates a ZIP file with all [Project] data and uploads it to the Akrab server for storing
	 * and making it available in Web front-ends. Overwrites any [Project] data already present at the
	 * server, therefore the user must have confirmed this action.
	 *
	 * @throws AkrabApiException containing an [AkrabApiError] in case of an error
	 */
	suspend fun upload(project: Project) {
		LOG.userTrail("Uploading project ${project.uuid}")

		if (!Session.exists) {
			throw IllegalStateException("no session")
		}

		val tempFilePath = Files.createTempFile(null, ".zip")
		FileOutputStream(tempFilePath.absolutePathString()).use {
			persistenceService.exportLibrary(project.identification, it)
			it.flush()
		}

		val response = httpClient.post("$baseUrl/bundle") {
			setBody(
				MultiPartFormDataContent(
					formData {
						append("description", "Antares Project Bundle")
						append("bundle", tempFilePath.toFile().readBytes(), Headers.build {
							append(HttpHeaders.ContentType, "application/zip")
							append(HttpHeaders.ContentDisposition, "filename=\"Upload.acp\"")
						})
					}
				)
			)
		}

		if (response.status == HttpStatusCode.OK) {
			return
		}

		val body = response.bodyAsText()
		LOG.debug("Upload: Status = ${response.status}, body = '$body'")
		throw AkrabApiException(AkrabApiError(body))
	}
}