package ch.scorpion.jabbah.base


import ch.scorpion.jabbah.base.exception.NoSuchElementException

/**
 * Contains system-wide available properties defined as name/value pairs.
 *
 * Distinguishes between predefined system properties, which are not persistent, and persistent user-defined properties
 * that can overwrite the system properties.
 *
 * All explicit getter functions without a default parameter throw a [NoSuchElementException]
 * if the value is absent.
 *
 * TODO Provide methods to load and store user properties
 * TODO Design: Separate system properties and user settings
 */
open class Properties {

    private val LOG by logger(Properties::class)

    /** Contains the predefined system properties as well as the user properties. Not persistent.*/
    private val allProperties: MutableMap<String,Any> by lazy { mutableMapOf<String,Any>() }

    /** Contains only the user properties. Persistent.*/
    private val userProperties: MutableMap<String,Any> by lazy { mutableMapOf<String,Any>() }

    /** Resets this [Properties] object by clearing all contained properties. Uses for testing.*/
    fun reset() {
        allProperties.clear()
        userProperties.clear()
    }

    fun copyFrom(p: Properties) {
        allProperties.putAll(p.allProperties)
        userProperties.putAll(p.userProperties)
    }

    /** Adds a predefined system property. Typically called at application start-up.*/
    fun predefine(name: String, value: Any) {
        allProperties.put(name, value)
    }

    /** Adds a user-defined property.*/
    fun set(name: String, value: Any) {
        LOG.debug("Setting $name to $value")
        userProperties.put(name, value)
        allProperties.put(name, value)
    }

    fun getString(name: String, defaultValue: String? = null): String {
        return get(name, defaultValue)
    }

    @Suppress("unused")
    fun getBoolean(name: String, defaultValue: Boolean? = null): Boolean {
        return get(name, defaultValue)
    }

    fun getInt(name: String, defaultValue: Int? = null): Int {
        val value = get(name) ?: return defaultValue!!
        if (value is Int) {
            return value
        }
        return (value as String).toInt()
    }

    fun getFloat(name: String, defaultValue: Float? = null): Float {
        return get(name, defaultValue)
    }

    fun get(name: String): Any? {
        return allProperties.get(name)
    }

    fun getUserPropertyKeys(): Iterator<String> {
        return userProperties.keys.iterator()
    }

    /** TODO This should really be in a separate Settings class.*/
    fun getBooleanSetting(name: String, default: Boolean): Boolean {
        val setting: String? = getOptional(name)
        if (StringUtils.isEmpty(setting)) {
            return default
        }
        return setting?.toUpperCase() == "TRUE"
    }

    protected fun <T> get(name: String, defaultValue: T? = null): T {
        val value = get(name) as T
        if (value != null) {
            return value
        }
        if (defaultValue != null) {
            return defaultValue
        }
        throw NoSuchElementException("no property '$name'")
    }

    protected fun <T> getOptional(name: String): T? {
        return get(name) as T
    }
}