package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import io.ktor.util.*
import io.ktor.utils.io.core.internal.*
import kotlinx.serialization.json.Json
import java.net.URL

/**
 * A client-side (JVM) service for calling Akrab REST endpoints of [Project].
 */
class ProjectAkrabClientServiceJvm(
	private val baseUrl: URL,
	private val persistenceService: FileLibraryPersistenceService
) {
	companion object {
		private val LOG by logger(ProjectAkrabClientServiceJvm::class)

		fun getError(text: String): AkrabApiError {
			try {
				return Json.decodeFromString(text)
			} catch (e: Throwable) {
				throw IllegalArgumentException("not a parsable error")
			}
		}
	}

	/*
	private val client: HttpClient by lazy {
		HttpClient(Java) {
			install(JsonFeature) {
				val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
				serializer = KotlinxSerializer(json)
			}
			HttpResponseValidator {
				handleResponseException { cause ->
					when (cause) {
						is ClientRequestException -> getError(cause.response.readText()).also {
							throw AkrabApiException(it)
						}
						else -> throw cause
					}
				}
			}
		}
	}
	*/


	/**
	 * Creates a ZIP file with all [Project] data and uploads it to the Akrab server for storing
	 * and making it available in Web front-ends. Overwrites any [Project] data already present at the
	 * server, therefore the user must have confirmed this action.
	 *
	 * @throws AkrabApiException containing an [AkrabApiError] in case of an error
	 */
	@OptIn(KtorExperimentalAPI::class, DangerousInternalIoApi::class)
	suspend fun upload(project: Project) {
		TODO()
		/*
		LOG.userTrail("Uploading project ${project.uuid}")

		if (!Auth0Session.exists) {
			throw IllegalStateException("no session")
		}

		val tempFilePath = Files.createTempFile(null, ".zip")
		FileOutputStream(tempFilePath.absolutePathString()).use {
			persistenceService.exportLibrary(project.identification, it)
			it.flush()
			it.close()
		}

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

		client.submitFormWithBinaryData<HttpResponse>(formData = parts) {
			url("$baseUrl/api/projectBundles")
			headers {
				append(HttpHeaders.Authorization, "Bearer ${Auth0Session.loginInfo!!.accessToken}")
			}
		}
		*/
	}
}