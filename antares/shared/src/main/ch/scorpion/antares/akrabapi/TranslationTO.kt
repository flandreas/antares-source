package ch.scorpion.antares.akrabapi

import kotlinx.serialization.Serializable

@Serializable
data class TranslationTO(
	val language: String,
	val text: String
)

@Serializable
data class TranslationsTO(
	val translations: List<TranslationTO>
) {
	companion object {
		fun of(text: String): TranslationsTO {
			return TranslationsTO(listOf(TranslationTO("en", text)))
		}
	}

	fun getText(): String = translations.firstOrNull()?.text ?: "<empty>"
}