package ch.scorpion.jabbah.base


import ch.scorpion.jabbah.base.exception.NoSuchElementException

/**
 * Contains system-wide available properties defined as name/value pairs.
 *
 * All explicit getter functions without a default parameter throw a [NoSuchElementException]
 * if the value is absent.
 */
open class Properties {

    companion object {
        private val LOG by logger(Properties::class)
    }

    private val values: MutableMap<String,Any> by lazy { mutableMapOf<String,Any>() }

    fun getString(name: String): String {
        return get(name)
    }

    fun getBoolean(name: String): Boolean {
        return get(name)
    }

    fun getInt(name: String): Int {
        return get(name)
    }

    fun getFloat(name: String): Float {
        return get(name)
    }

    fun copyFrom(p: Properties) {
        values.putAll(p.values)
    }

    /** Adds a predefined system property. Typically called at application start-up.*/
    fun set(name: String, value: Any) {
        values.put(name, value)
    }

    fun <T> get(name: String): T {
        return getOptional<T>(name) ?: throw NoSuchElementException("no property '$name'")
    }

    protected fun <T> getOptional(name: String): T? {
        return values.get(name) as T?
    }
}

class Settings {

    companion object {
        private val LOG by logger(Settings::class)
    }

    private val values: MutableMap<String, String> by lazy { mutableMapOf<String,String>() }

    fun getString(name: String, defaultValue: String): String {
        return getOptional(name) ?: defaultValue
    }

    fun getBoolean(name: String, defaultValue: Boolean): Boolean {
        val setting: String? = getOptional(name)
        if (StringUtils.isEmpty(setting)) {
            return defaultValue
        }
        return setting?.toUpperCase() == "TRUE"
    }

    fun getInt(name: String, defaultValue: Int): Int {
        return getOptional(name)?.toInt() ?: defaultValue
    }

    fun getFloat(name: String, defaultValue: Float): Float {
        throw UnsupportedOperationException("not implemented")
    }

    fun getKeys(): Iterator<String> {
        return values.keys.iterator()
    }

    fun get(name: String): String {
        return values[name]!!
    }

    /** Adds a user-defined property.*/
    fun set(name: String, value: Any) {
        LOG.debug("Setting $name to $value")
        values.put(name, value.toString())
    }

    private fun getOptional(name: String): String? {
        return values[name]
    }
}