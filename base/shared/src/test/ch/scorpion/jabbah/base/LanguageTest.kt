package ch.scorpion.jabbah.base

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Locale

/** Unit tests for [Language].*/
class LanguageTest {

	@Before
	fun setup() {
		LOG_SYSTEM = LogSystemJVM()
		Translations = TranslationsJvm()
		Locale.setDefault(Locale("en", "US"))
		Translations.addBundle("jabbah-base")
	}

	@Test
	fun shouldYieldForCode() {
		assertThat(Language.withCode("en"), `is`(Language.English))
	}

	@Test
	fun shouldTranslateToString() {
		assertThat(Language.English.toString(), `is`("English"))
		assertThat(Language.German.toString(), `is`("German"))
	}
}