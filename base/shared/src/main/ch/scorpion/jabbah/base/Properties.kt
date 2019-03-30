package ch.scorpion.jabbah.base


import ch.scorpion.jabbah.base.exception.NoSuchElementException

/**
 * Contains system-wide available properties defined as name/value pairs.
 *
 * Properties that are initially established by the application at startup are not made persistent.
 * Those properties can be customized by the user (also known as "preferences"), and these customized
 * properties are made persistent.
 *
 * All explicit getter functions without a default parameter throw a [NoSuchElementException]
 * if the value is absent.
 */
open class Properties {

	/**
	 * Represents an individual entry of [Properties] by distinguishing between different states.
	 * @property stringValue the persistent [String] representation of the property's value
	 * @property objValue the typed representation of the property's value. Not available until the property
	 * is accesses for the first time using a typed getter method.
	 */
	data class Entry(val stringValue: String, val objValue: Any?, val customized: Boolean)

	val size: Int get() = entries.size

	protected val entries: MutableMap<String,Entry> by lazy { mutableMapOf<String,Entry>() }

	fun getKeys(): Iterator<String> {
		return entries.keys.iterator()
	}

	fun contains(name: String): Boolean {
		return entries.containsKey(name)
	}

	fun clear() {
		entries.clear()
	}

	fun remove(name: String): Properties {
		entries.remove(name)
		return this
	}

	fun copyFrom(p: Properties) {
		entries.putAll(p.entries)
	}

	fun copyTo(p: Properties) {
		p.entries.putAll(entries)
	}

	/**
	 * Adds a predefined system property.
	 * Used to register a default values for a property. Typically called at application start-up,
	 * where the type of the value is known.
	 */
	open fun set(name: String, value: Any) {
		entries[name] = Entry(value.toString(), value, false)
	}

	fun getCustomizedKeys(): Iterator<String> {
		return entries.keys.filter { entries[it]!!.customized }.iterator()
	}

	/** Customizes a predefined system property. Typically called by a UI.*/
	open fun customize(name: String, value: Any) {
		if (value != entries[name]?.objValue) {
			entries[name] = Entry(value.toString(), value, customized = true)
		}
	}

	/** Initially loads a property using its persistent [String] representation.*/
	fun load(name: String, value: String) {
		entries[name] = Entry(value, null, customized = true)
	}


	fun getBoolean(name: String): Boolean {
		var entry = getEntry(name)
		if (entry.objValue == null) {
			entry = entry.copy(objValue = entry.stringValue.toUpperCase() == "TRUE")
			entries[name] = entry
		}
		return entry.objValue as Boolean
	}

	fun getString(name: String): String {
		var entry = getEntry(name)
		if (entry.objValue == null) {
			entry = entry.copy(objValue = entry.stringValue)
			entries[name] = entry
		}
		return entry.objValue as String
	}

	fun getInt(name: String): Int {
		var entry = getEntry(name)
		if (entry.objValue == null) {
			entry = entry.copy(objValue = entry.stringValue.toInt())
			entries[name] = entry
		}
		return entry.objValue as Int
	}

	fun getFloat(name: String): Float {
		var entry = getEntry(name)
		if (entry.objValue == null) {
			entry = entry.copy(objValue = entry.stringValue.toFloat())
			entries[name] = entry
		}
		return entry.objValue as Float
	}

	open fun getEntry(name: String): Entry {
		return getOptionalEntry(name) ?: throw NoSuchElementException("no property '$name'")
	}

	open fun getOptionalEntry(name: String): Entry? {
		return entries[name]
	}

	fun <T> get(name: String): T {
		return getEntry(name).objValue as T
	}

	fun <T> getOptional(name: String): T? {
		return getOptionalEntry(name)?.objValue as T?
	}
}


open class PropertiesProxy(protected val target: Properties) : Properties() {

	override fun getOptionalEntry(name: String): Entry? {
		return target.getOptionalEntry(name)
	}

	override fun set(name: String, value: Any) {
		target.set(name, value)
	}

	override fun customize(name: String, value: Any) {
		target.set(name, value)
	}
}

/**
 * Contains settings the user changes while using the system, such as position and size of the main application window.
 * These settings are made persistent, and are re-established the next time the application is used.
 */
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

	fun getIntegers(name: String): List<Int> {
		return getOptional(name)
			?.split(",")
			?.filter { it.isNotBlank() }
			?.map { it.toInt() }
			?: listOf()
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

	fun set(name: String, integers: Iterable<Int>) {
		values[name] = integers.joinToString(",")
	}

    fun remove(name: String) {
        values.remove(name)
    }

    private fun getOptional(name: String): String? {
        return values[name]
    }
}