package ch.scorpion.jabbah.base

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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