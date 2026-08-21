package io.antarescircuit.antares.ai

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A single message of the chat conversation sent to the model. */
@Serializable
data class OpenRouterMessage(val role: String, val content: String) {
	companion object {
		const val ROLE_SYSTEM = "system"
		const val ROLE_USER = "user"
		const val ROLE_ASSISTANT = "assistant"

		fun system(content: String) = OpenRouterMessage(ROLE_SYSTEM, content)
		fun user(content: String) = OpenRouterMessage(ROLE_USER, content)
		fun assistant(content: String) = OpenRouterMessage(ROLE_ASSISTANT, content)
	}
}

/** Raised for every failure of a chat completion call. The message is displayable to the user. */
class OpenRouterException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * A minimal client for the OpenRouter chat completions endpoint.
 *
 * Uses a dedicated [HttpClient] because model responses regularly take much longer than the
 * application-wide connection timeout used for the Antares backend.
 */
class OpenRouterClient(
	private val url: String = OpenRouterConfig.CHAT_COMPLETIONS_URL,
	private val modelProvider: () -> String = OpenRouterConfig::model,
) {

	companion object {

		private val LOG by logger(OpenRouterClient::class)

		/** Generous timeout: model answers can take a while, and the call is cancellable by the user.*/
		private const val TIMEOUT_MS = 180_000

		private val client: HttpClient by lazy {
			HttpClient(Apache) {
				engine {
					followRedirects = true
					socketTimeout = TIMEOUT_MS
					connectTimeout = 30_000
					connectionRequestTimeout = 30_000
				}
				install(ContentNegotiation) {
					// encodeDefaults, because response_format only exists as a default value
					json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
				}
			}
		}
	}

	/**
	 * Sends [messages] to the model and returns the raw content of the first choice.
	 * @throws OpenRouterException on any transport, authentication or protocol level problem
	 */
	suspend fun complete(messages: List<OpenRouterMessage>, apiKey: String): String =
		withContext(Dispatchers.IO) {
			val response = try {
				client.post(url) {
					contentType(ContentType.Application.Json)
					header(HttpHeaders.Authorization, "Bearer $apiKey")
					header("HTTP-Referer", OpenRouterConfig.REFERER)
					header("X-Title", OpenRouterConfig.TITLE)
					setBody(ChatRequest(model = modelProvider(), messages = messages))
				}
			} catch (e: Exception) {
				LOG.error("OpenRouter request failed: ${e.message}")
				throw OpenRouterException(Translations.getString("antares.ai.error.network", e.message ?: e::class.simpleName ?: ""), e)
			}

			if (!response.status.isSuccess()) {
				throw OpenRouterException(describeFailure(response))
			}

			val body = try {
				response.body<ChatResponse>()
			} catch (e: Exception) {
				LOG.error("Unreadable OpenRouter response: ${e.message}")
				throw OpenRouterException(Translations.getString("antares.ai.error.badResponse"), e)
			}

			body.error?.message?.let { throw OpenRouterException(it) }

			body.choices.firstOrNull()?.message?.content
				?: throw OpenRouterException(Translations.getString("antares.ai.error.emptyResponse"))
		}

	private suspend fun describeFailure(response: HttpResponse): String {
		val detail = try {
			response.bodyAsText().take(500)
		} catch (e: Exception) {
			""
		}
		LOG.error("OpenRouter returned ${response.status}: $detail")
		return when (response.status) {
			HttpStatusCode.Unauthorized, HttpStatusCode.PaymentRequired, HttpStatusCode.Forbidden ->
				Translations.getString("antares.ai.error.unauthorized", response.status.value)
			HttpStatusCode.TooManyRequests ->
				Translations.getString("antares.ai.error.rateLimit")
			else ->
				Translations.getString("antares.ai.error.status", response.status.value, detail)
		}
	}

	@Serializable
	private data class ChatRequest(
		val model: String,
		val messages: List<OpenRouterMessage>,
		// No temperature: the GPT-5 model family rejects any value other than its default
		@SerialName("response_format") val responseFormat: ResponseFormat = ResponseFormat()
	)

	@Serializable
	private data class ResponseFormat(val type: String = "json_object")

	@Serializable
	private data class ChatResponse(
		val choices: List<Choice> = emptyList(),
		val error: ApiError? = null
	)

	@Serializable
	private data class Choice(val message: OpenRouterMessage? = null)

	@Serializable
	private data class ApiError(val message: String? = null)
}
