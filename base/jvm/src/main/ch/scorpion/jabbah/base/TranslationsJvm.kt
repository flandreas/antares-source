package ch.scorpion.jabbah.base

import java.text.MessageFormat
import java.util.*

/**
 * Implements [Translations] using [PropertyResourceBundle]s.
 */
actual object Translations {

    val LOG by logger(Translations::class)

	private var withAllKeys = false

	actual var language: Language
		get() = System.currentLanguage()
		set(value) {
			if (language != value) {
				handleLanguageChanged(value.code)
			}
		}

	actual fun withAnyKey() {
		withAllKeys = true
	}

    actual fun getString(key: String, vararg params: Any): String =
	    MessageFormat.format(getString(key, optional = false), *params)

	actual fun getOptionalString(key: String): String? = getString(key, optional = true)

	actual fun addBundle(name: String) {
		bundleNames.add(name)
        addBundle(ResourceBundle.getBundle(name))
    }

    /** ---- [Translations] */

    private val bundleNames: MutableList<String> = mutableListOf()
    private val bundles: MutableList<ResourceBundle> = mutableListOf()

    fun addBundle(bundle: ResourceBundle) {
        if (!bundles.contains(bundle)) {
            bundles.add(0, bundle)}
    }

    fun clear() {
	    bundleNames.clear()
        bundles.clear()
    }

    private fun getString(key: String, optional: Boolean): String? {
        for (bundle in bundles) {
            if (bundle.containsKey(key)) {
                return bundle.getString(key)
            }
        }
        if (optional) {
            return null
        }

	    if (withAllKeys) {
			return key
	    }

        LOG.debug("Missing translation '$key'")
        throw MissingResourceException("Missing translation", Translations::class.java.name, key)
    }

	private fun handleLanguageChanged(languageCode: String) {
		java.lang.System.setProperty("user.language", languageCode)
		bundles.clear()
		bundleNames.forEach { addBundle(ResourceBundle.getBundle(it, Locale(languageCode))) }
	}
}