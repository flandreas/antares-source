package ch.scorpion.jabbah.base

import java.text.MessageFormat
import java.util.*

/**
 * Implements [Translations] using [PropertyResourceBundle]s.
 */
actual object Translations {

    val LOG by logger(Translations::class)

	private var withAllKeys = false

	actual fun withAnyKey() {
		withAllKeys = true
	}

    actual fun getString(key: String, vararg params: Any): String =
	    MessageFormat.format(getString(key, optional = false), *params)

	actual fun getOptionalString(key: String): String? = getString(key, optional = true)

	actual fun addBundle(name: String) {
        addBundle(ResourceBundle.getBundle(name))
    }

    /** ---- [Translations] */

    private val bundles: MutableList<ResourceBundle> = mutableListOf<ResourceBundle>()

    fun addBundle(bundle: ResourceBundle) {
        if (!bundles.contains(bundle)) {
            bundles.add(0, bundle)}
    }

    fun clear() {
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
}