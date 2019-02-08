package ch.scorpion.jabbah.base

import io.mockk.every
import io.mockk.mockk

/**
 * Testing utility class that adds additional translations to [TranslationsJvm].
 */
class TestTranslationsBuilder() {

	private val translations = mockk<TranslationsClass>()

	fun withAnyKey() {
		every { translations.getString(any()) } returns "AnyString"
		every { translations.getOptionalString(any()) } returns "AnyString"
		Translations = translations
	}
}