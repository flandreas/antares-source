package io.antarescircuit.antares.ai

/**
 * The platform-neutral defaults of the OpenRouter connection of the circuit assistant, shared by
 * the desktop ([OpenRouterConfig]) and the web editor.
 */
object OpenRouterDefaults {

	const val CHAT_COMPLETIONS_URL = "https://openrouter.ai/api/v1/chat/completions"

	/** The model used by the circuit assistant unless the user configures another one.*/
	const val MODEL = "openai/gpt-5.6-luna"

	/** Sent by OpenRouter conventions to identify the calling application.*/
	const val REFERER = "https://www.antarescircuit.io"
	const val TITLE = "Antares AI Assistant"
}
