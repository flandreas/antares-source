package ch.scorpion.jabbah.base

/**
 * Implements [TranslationsClass] for the JavaScript target.
 */
class TranslationsJs : TranslationsClass() {

    override fun addBundle(name: String) {
        // empty
    }

    override fun getString(key: String, vararg params: Any): String {
        return key
    }

    override fun getOptionalString(key: String): String? {
        return key
    }
}