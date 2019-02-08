package ch.scorpion.jabbah.base

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import java.util.Locale

/** Unit tests for [Language].*/
class LanguageTest {

	@BeforeTest
	fun setup() {
		LOG_SYSTEM = LogSystemJVM()
		Translations = TranslationsJvm()
		Locale.setDefault(Locale("en", "US"))
		Translations.addBundle("jabbah-base")
	}

	@Test
	fun shouldYieldForCode() {
		assertEquals(Language.withCode("en"), Language.English)
	}

	@Test
	fun shouldTranslateToString() {
		assertEquals(Language.English.toString(), "English")
		assertEquals(Language.German.toString(), "German")
	}
}