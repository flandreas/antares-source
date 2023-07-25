package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.richtext.RichTextParser
import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.graphics.FontImpl
import kotlin.test.BeforeTest
import kotlin.test.Test

class RichTextDrawableTest {

	@BeforeTest
	fun beforeTest() {
		DrawTestRule.configure()
	}

	@Test
	fun shouldCreateDrawable() {
		val richText = RichTextParser("Abc").parse()
		RichTextDrawableTransformer(richText, FontImpl()).transform()
	}
}