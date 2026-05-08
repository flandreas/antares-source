package io.antarescircuit.jabbah.base

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import java.util.Locale

/** Unit tests for [Language].*/
class LanguageTest {

	@BeforeTest
	fun setup() {
		Locale.setDefault(Locale.of("en", "US"))
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