package io.antarescircuit.antares.ai

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule

/**
 * Resolves the configuration of the OpenRouter connection.
 *
 * The API key is never part of the source code or of any repository. It is taken from the
 * `OPENROUTER_API_KEY` environment variable, which takes precedence, or from the user preference
 * that can be edited in the preferences dialog.
 */
object OpenRouterConfig {

	/** The environment variable that provides the API key.*/
	const val ENV_API_KEY = "OPENROUTER_API_KEY"

	/** The name of the [String] preference holding the API key entered in the preferences dialog.*/
	const val PROP_API_KEY = "antares.ai.apiKey"

	/** The name of the [String] preference holding the OpenRouter model identifier.*/
	const val PROP_MODEL = "antares.ai.model"

	const val CHAT_COMPLETIONS_URL = "https://openrouter.ai/api/v1/chat/completions"

	/** The model used by the circuit assistant unless the user configures another one.*/
	const val MODEL = "openai/gpt-5.6-luna"

	/** Sent by OpenRouter conventions to identify the calling application.*/
	const val REFERER = "https://www.antarescircuit.io"
	const val TITLE = "Antares AI Assistant"

	/** Where the currently used API key comes from. */
	enum class KeySource { Environment, Preferences, None }

	/** Registers the default values of the OpenRouter preferences. Called during module bootstrap. */
	fun fillProperties(properties: Properties) {
		properties.set(PROP_API_KEY, "")
		properties.set(PROP_MODEL, MODEL)
	}

	fun keySource(properties: Properties = BaseModule.properties): KeySource = when {
		!environmentKey().isNullOrBlank() -> KeySource.Environment
		preferenceKey(properties).isNotBlank() -> KeySource.Preferences
		else -> KeySource.None
	}

	/** Returns the API key to use, or `null` if none is configured. */
	fun apiKey(properties: Properties = BaseModule.properties): String? =
		environmentKey()?.takeIf { it.isNotBlank() }
			?: preferenceKey(properties).takeIf { it.isNotBlank() }

	/** Returns the configured model, falling back to [MODEL] for a blank preference. */
	fun model(properties: Properties = BaseModule.properties): String =
		properties.getString(PROP_MODEL).trim().ifBlank { MODEL }

	private fun environmentKey(): String? = System.getenv(ENV_API_KEY)?.trim()

	private fun preferenceKey(properties: Properties): String =
		properties.getString(PROP_API_KEY).trim()
}
