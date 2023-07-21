package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.assertAST
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

class RichTextParserTest {

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
	}

	@Ignore
	@Test
	fun shouldParseText() {
		assertAST(RichTextParser("This is a text").parse(), """
			Compound
			- This is a text
		""".trimIndent())
	}
}