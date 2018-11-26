package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.MissingResourceException

/**
 * Provides I18N translations of static texts.
 */
@Suppress("UNUSED_PARAMETER")
open class TranslationsClass {

    /**
     * Adds the resource bundle with the specified name.
     */
    open fun addBundle(name: String): Unit = throw NotImplementedError()

    /**
     * Retrieves the translation with the specified key and substitutes variables with the provided parameters.
     * @throws MissingResourceException if no translation for [key] was found
     */
    open fun getString(key: String, vararg params: Any): String = throw NotImplementedError()

    /**
     * Retrieves the translation with the specified key, or returns `null` if it doesn't exist.
     */
    open fun getOptionalString(key: String): String? = throw NotImplementedError()
}

var Translations: TranslationsClass = TranslationsClass()

/**
 * Defines all languages supported by the Jabbah framework for translation of dynamic (i.e. user provided) text.
 * @property code the ISO 639-1 two-letter language code. Example: "en" for English.
 */
enum class Language(val code: String) {
	English("en"),
	German("de");

	companion object {

		val DEFAULT: Language = English

		fun withCode(code: String): Language {
			return getLanguage(code) ?: throw IllegalArgumentException("unknown Language $code")
		}

		fun supports(code: String): Boolean {
			return getLanguage(code) != null
		}

		private fun getLanguage(code: String): Language? {
			for (lang in Language.values()) {
				if (lang.code == code) {
					return lang
				}
			}
			return null
		}
	}

	override fun toString(): String = when(this) {
		Language.English -> Translations.getString("base.language.en.name")
		Language.German -> Translations.getString("base.language.de.name")
	}
}
