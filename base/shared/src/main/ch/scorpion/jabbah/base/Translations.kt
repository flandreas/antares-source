package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.MissingResourceException

/**
 * Provides I18N translations of static texts.
 */
@Suppress("UNUSED_PARAMETER")
expect object Translations {

	var language: Language

	/** Returns the key if no value is found. Primarily used for testing.*/
	fun withAnyKey()

	/**
	 * Adds the resource bundle with the specified name.
	 */
	fun addBundle(name: String)

	/**
	 * Retrieves the translation with the specified key and substitutes variables with the provided parameters.
	 * @throws MissingResourceException if no translation for [key] was found
	 */
	fun getString(key: String, vararg params: Any): String

	/**
	 * Retrieves the translation with the specified key, or returns `null` if it doesn't exist.
	 */
	fun getOptionalString(key: String): String?
}

/**
 * Defines all languages supported by the Jabbah framework for translation of dynamic (i.e. user provided) text.
 * @property code the ISO 639-1 two-letter language code. Example: "en" for English.
 */
enum class Language(val code: String) {
	English("en"),
	German("de");

	companion object {

		val DEFAULT: Language = English
		const val PROP_LANGUAGE = "base.language"

		fun withCode(code: String): Language {
			return getLanguage(code) ?: throw IllegalArgumentException("unknown Language $code")
		}

		fun supports(code: String): Boolean {
			return getLanguage(code) != null
		}

		private fun getLanguage(code: String): Language? {
			for (lang in values()) {
				if (lang.code == code) {
					return lang
				}
			}
			return null
		}
	}

	val isDefault: Boolean get() = this === DEFAULT

	val isNonDefault: Boolean get() = this !== DEFAULT

	override fun toString(): String = when (this) {
		English -> Translations.getString("base.language.en.name")
		German -> Translations.getString("base.language.de.name")
	}
}
