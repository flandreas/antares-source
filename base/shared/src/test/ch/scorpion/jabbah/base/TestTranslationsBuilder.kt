package ch.scorpion.jabbah.base

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import java.util.*

/**
 * Testing utility class that adds additional translations to [TranslationsJvm].
 */
class TestTranslationsBuilder(clear: Boolean) {
    constructor(): this(false)

    private val resourceBundle: PropertyResourceBundle = mock()

    init {
        if (clear) {
            (Translations as TranslationsJvm).clear()
        }
        (Translations as TranslationsJvm).addBundle(resourceBundle)
    }

    fun withAnyKey() {
        whenever(resourceBundle.containsKey(any())).thenReturn(true)
        whenever(resourceBundle.handleGetObject(any())).thenReturn("AnyString")
    }

    fun withResource(key: String): TestTranslationsBuilder {
        whenever(resourceBundle.containsKey(any())).thenReturn(true)
        whenever(resourceBundle.handleGetObject(key)).thenReturn("translation of $key")
        return this
    }
}