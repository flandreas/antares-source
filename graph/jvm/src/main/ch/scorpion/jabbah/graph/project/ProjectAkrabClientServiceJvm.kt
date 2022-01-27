package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.auth0.Auth0Session
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.streams.*
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
	 * @return `true` if upload was successful
	 */
	@OptIn(KtorExperimentalAPI::class)
	suspend fun upload(project: Project): Boolean {
		LOG.debug("Uploading project ${project.uuid}")

		if (!Auth0Session.exists) {
			return false
		}

		val tempFilePath = Files.createTempFile(null, ".zip")
		FileOutputStream(tempFilePath.absolutePathString()).use {
			persistenceService.exportLibrary(project.uuid, it)
			it.flush()
			it.close()
		}

		try {

			val parts: List<PartData> = formData {
				val headersBuilder = HeadersBuilder()
				headersBuilder[HttpHeaders.ContentType] = "application/zip"
				headersBuilder[HttpHeaders.ContentDisposition] = "filename=projectUpload.zip"
				this.append(
					"file",
					InputProvider { tempFilePath.toFile().inputStream().asInput() },
					headersBuilder.build()
				)
			}

			val response: HttpResponse = BaseModuleJvm.httpClient.submitFormWithBinaryData(formData = parts) {
				url("$baseUrl/api/projectBundles")
				headers {
					append(HttpHeaders.Authorization, "Bearer ${Auth0Session.loginInfo!!.accessToken}")
				}
			}

			return response.status == HttpStatusCode.NoContent
		} catch (e: Throwable) {
			LOG.error("Error while uploading project: ${e.message}")
			return false
		}
	}
}