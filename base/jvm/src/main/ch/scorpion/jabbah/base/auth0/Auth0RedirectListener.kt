package ch.scorpion.jabbah.base.auth0

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.logger
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

class Auth0RedirectListener(
	private val params: LoginParams,
	private val flowInfo: FlowInfo
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

		LOG.info("Listening on host=$host, port=$port, path=$path for redirect")

		Spark.get(path) { request, response ->
			LOG.info("Received Request Query: ${request.queryString()}")

			val code = request.queryParams("code")
			val state = request.queryParams("state")

			if (flowInfo.state == state && StringUtils.isNotEmpty(code)) {
				fetchLoginInfo(flowInfo, code)
			}

			response.body("Response")
		}

		Spark.awaitInitialization()
	}

	fun stop() {
		Spark.stop()
	}

	private fun fetchLoginInfo(flowInfo: FlowInfo, code: String) {
		LOG.info(("Fetching LoginInfo..."))

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

		val response = client.send(request, HttpResponse.BodyHandlers.ofString())
		if (response.statusCode() == 200) {
			val format = Json { ignoreUnknownKeys = true }
			Auth0Session.establish(format.decodeFromString(response.body()))
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