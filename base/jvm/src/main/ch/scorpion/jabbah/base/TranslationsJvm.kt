package ch.scorpion.jabbah.base

import java.text.MessageFormat
import java.util.*

/**
 * Implements [TranslationsClass] using [PropertyResourceBundle]s.
 */
open class TranslationsJvm : TranslationsClass() {

    val LOG by logger()

    override fun getString(key: String, vararg params: Any): String {
        return MessageFormat.format(getString(key, optional = false), *params)
    }

    override fun getOptionalString(key: String): String? {
        return getString(key, optional = true)
    }

    override fun addBundle(name: String) {
        addBundle(ResourceBundle.getBundle(name))
    }

    /** ---- [TranslationsJvm] */

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
        LOG.debug("Missing translation '$key'")
        throw MissingResourceException("Missing translation", TranslationsJvm::class.java.name, key)
    }
}