package ch.scorpion.antares

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.TranslationsJvm
import io.mockk.every
import io.mockk.mockk
import java.util.*

/**
 * Testing utility class that adds additional translations to [TranslationsJvm].
 * TODO Copy of the corresponding class from the base module. Was not able to reference that
 * in the gradle build.
 */
class TestTranslationsBuilder(clear: Boolean) {
	constructor() : this(false)

	private val resourceBundle: PropertyResourceBundle = mockk(relaxed = true)

	init {
		if (clear) {
			(Translations as TranslationsJvm).clear()
		}
		(Translations as TranslationsJvm).addBundle(resourceBundle)
	}

	fun withAnyKey() {
		every { resourceBundle.containsKey(any()) } returns true
		every { resourceBundle.handleGetObject(any()) } returns "AnyString"
	}

	fun withResource(key: String): TestTranslationsBuilder {
		every { resourceBundle.containsKey(any()) } returns true
		every { resourceBundle.handleGetObject(key) } returns "translation of $key"
		return this
	}
}