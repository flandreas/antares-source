package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
	 */
	@OptIn(KtorExperimentalAPI::class)
	suspend fun upload(project: Project): Boolean {
		LOG.debug("Uploading project ${project.uuid}")

		val tempFilePath = Files.createTempFile(null, "zip")
		FileOutputStream(tempFilePath.absolutePathString()).use {
			persistenceService.exportLibrary(project.uuid, it)
		}

		val headersBuilder = HeadersBuilder()
		headersBuilder.append(HttpHeaders.ContentType, "application/zip")

		val response : HttpResponse = BaseModuleJvm.httpClient.submitFormWithBinaryData {
			url("$baseUrl/api/projectBundles")
			formData {
				append(
					"file",
					InputProvider { tempFilePath.toFile().inputStream().asInput() },
					headersBuilder.build()
				)
			}
		}

		return response.status == HttpStatusCode.NoContent
	}
}