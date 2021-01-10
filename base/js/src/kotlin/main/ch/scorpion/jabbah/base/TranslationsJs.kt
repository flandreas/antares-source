package ch.scorpion.jabbah.base

/**
 * Implements [Translations] for the JavaScript target.
 */
actual object Translations {

	private val map = mutableMapOf<String,String>()

	actual var language: Language
		get() = System.currentLanguage()
		set(@Suppress("UNUSED_PARAMETER") value) {
			throw NotImplementedError()
		}


	actual fun withAnyKey() {
		// empty
	}

	actual fun addBundle(name: String) {
        // empty
    }

	actual fun addKey(key: String, value: String) {
		map[key] = value
	}

	actual fun getString(key: String, vararg params: Any): String = map.getOrElse(key, { key } )

	actual fun getOptionalString(key: String): String? = map[key]
}