package ch.scorpion.jabbah.base.auth0

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import spark.Spark
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

/**
 * Starts a Spark Server for listening on the loop-back interface for the redirect from Auth0
 * that contains the access code for fetching the access token from Auth0.
 *
 * Establishes [Auth0Session] if everything went well.
 * Posts [Auth0LoginError] on [eventBus] if an error occurs.
 */
class Auth0RedirectListener(
	private val scope: CoroutineScope,
	private val params: LoginParams,
	private val flowInfo: FlowInfo,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	companion object {
		private val LOG by logger(Auth0RedirectListener::class)
	}

	fun start() {
		val url = URL(params.redirectUrl)
		val host = url.host
		val port = url.port
		val path = url.path

		Spark.ipAddress(host)
		Spark.port(port)
		Spark.threadPool(8)
		Spark.internalServerError("Internal Server Error 500")

		Spark.get(path) { request, _ ->
			val code = request.queryParams("code")
			val state = request.queryParams("state")

			if (flowInfo.state == state && StringUtils.isNotEmpty(code)) {
				scope.launch(Dispatchers.Main) {
					try {
						fetchLoginInfo(flowInfo, code)
					} catch (e: Throwable) {
						LOG.error("Exception while fetching access token from Auth0", e)
						eventBus.post(Auth0LoginError(Translations.getString("base.action.login.error.msg")))
					}
				}
			}

			Translations.getString("base.action.login.browserDone.txt")
		}

		// Blocking call
		Spark.awaitInitialization()
	}

	fun stop() {
		Spark.stop()
	}

	private suspend fun fetchLoginInfo(flowInfo: FlowInfo, code: String) {
		val uri = URI.create("https://${params.domain}/").resolve("/oauth/token")

		val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build()
		val request = HttpRequest
			.newBuilder()
			.POST(buildFormDataMap(mapOf(
				"grant_type" to "authorization_code",
				"client_id" to params.clientId,
				"code_verifier" to flowInfo.verifier,
				"code" to code,
				"redirect_uri" to params.redirectUrl
			)))
			.header("Content-Type", "application/x-www-form-urlencoded")
			.uri(uri)
			.build()

		val response = client
			.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.await()

		if (response.statusCode() == 200) {
			val format = Json { ignoreUnknownKeys = true }
			Auth0Session.establish(format.decodeFromString(response.body()))
		} else if (response.statusCode() == 401) {
			LOG.error("Received 401 while fetching access token from Auth0")
			eventBus.post(Auth0LoginError(Translations.getString("base.action.login.errorUnauthorized.msg")))
		} else {
			LOG.error("Received ${response.statusCode()} while fetching access token from Auth0")
			eventBus.post(Auth0LoginError(Translations.getString("base.action.login.error.msg")))
		}
	}

	private fun buildFormDataMap(data: Map<Any, Any>): HttpRequest.BodyPublisher {
		val builder = StringBuilder()
		for (entry in data) {
			if (builder.isNotEmpty()) {
				builder.append("&")
			}
			builder.append(URLEncoder.encode(entry.key.toString(), StandardCharsets.US_ASCII))
			builder.append("=")
			builder.append(URLEncoder.encode(entry.value.toString(), StandardCharsets.US_ASCII))
		}
		return HttpRequest.BodyPublishers.ofString(builder.toString())
	}
}