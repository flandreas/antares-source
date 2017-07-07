package ch.scorpion.jabbah.base

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
