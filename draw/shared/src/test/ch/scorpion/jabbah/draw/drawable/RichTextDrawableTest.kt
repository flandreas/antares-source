package ch.scorpion.jabbah.draw.drawable

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
		RichTextDrawable.of("Abc", FontImpl())
	}
}