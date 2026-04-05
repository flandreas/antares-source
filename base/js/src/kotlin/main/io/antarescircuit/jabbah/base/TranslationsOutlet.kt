package io.antarescircuit.jabbah.base

@Suppress("unused")
@JsExport
object TranslationsOutlet {

    fun getString(key: String): String =
        Translations.getString(key)

    fun getOptionalString(key: String): String? =
        Translations.getOptionalString(key)
}