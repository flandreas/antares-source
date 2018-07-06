package ch.scorpion.jabbah.base


import ch.scorpion.jabbah.base.exception.NoSuchElementException

/**
 * Contains system-wide available properties defined as name/value pairs.
 *
 * All explicit getter functions without a default parameter throw a [NoSuchElementException]
 * if the value is absent.
 */
open class Properties {

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
    open fun set(name: String, value: Any) {
	    values[name] = value
    }

    fun <T> get(name: String): T {
        return getOptional<T>(name) ?: throw NoSuchElementException("no property '$name'")
    }

    open fun <T> getOptional(name: String): T? {
	    @Suppress("UNCHECKED_CAST")
	    return values[name] as T?
    }
}

class Settings {

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
        return getOptional(name)?.toFloat() ?: defaultValue
    }

    fun getKeys(): Iterator<String> {
        return values.keys.iterator()
    }

    fun get(name: String): String {
        return values[name]!!
    }

    /** Adds a user-defined property.*/
    fun set(name: String, value: Any) {
	    values[name] = value.toString()
    }

    fun remove(name: String) {
        values.remove(name)
    }

    private fun getOptional(name: String): String? {
        return values[name]
    }
}