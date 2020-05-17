package ch.scorpion.jabbah.base

/**
 * Implements [Translations] for the JavaScript target.
 */
actual object Translations {

	actual var language: Language
		get() = System.currentLanguage()
		set(value) {
			throw NotImplementedError()
		}


	actual fun withAnyKey() {
		// empty
	}

	actual fun addBundle(name: String) {
        // empty
    }

	actual fun getString(key: String, vararg params: Any): String {
        return key
    }

	actual fun getOptionalString(key: String): String? {
        return key
    }
}