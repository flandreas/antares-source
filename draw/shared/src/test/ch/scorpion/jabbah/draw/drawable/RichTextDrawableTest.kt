package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.TextMeasurer
import ch.scorpion.jabbah.draw.graphics.TextRenderInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RichTextDrawableTest {

	companion object {
		private const val FONT_SIZE = 10
		private val FONT = FontImpl(size = FONT_SIZE)

		/** Measures the width of a text as the number of characters times the font size 10.*/
		private fun textMeasurer(): TextMeasurer {
			val textMeasurer = mockk<TextMeasurer>()

			val slot = slot<String>()
			every {
				textMeasurer.measureSingleLineText(text = capture(slot), font = any())
			} answers {
				TextRenderInfo(Rectangle2D(0, 0, FONT_SIZE * slot.captured.length, FONT_SIZE), 0.8 * FONT_SIZE)
			}

			return textMeasurer
		}
	}

	@BeforeTest
	fun beforeTest() {
		DrawTestRule.configure()
	}

	/** ---- Single line tests */

	@Test
	fun shouldCreateSingleLineDrawable() {
		RichTextDrawable.of("Abc", FONT)
	}

	/** ---- Multiline tests */

	@Test
	fun shouldWrapEveryWord() {
		val drawable = RichTextDrawable.multiline("This is some test", FONT, 2.0 * FONT_SIZE, textMeasurer())
		assertEquals(4 * FONT_SIZE + 3 * RichTextDrawable.LINE_DIST, drawable.height)

		// The longest word (4 chars) plus trailing blank is 5
		assertEquals(5 * FONT_SIZE, drawable.widthInt)
	}

	@Test
	fun shouldBreakAtNewline() {
		val drawable = RichTextDrawable.multiline("Line1\nLine2", FONT, 10.0 * FONT_SIZE, textMeasurer())
		assertEquals(2 * FONT_SIZE + 1 * RichTextDrawable.LINE_DIST, drawable.height)
	}

	@Test
	fun shouldRespectIndicesWidth() {
		val drawable = RichTextDrawable.multiline("*(A_(123)):B", FONT, 20.0 * FONT_SIZE, textMeasurer())
		assertEquals(6 * FONT_SIZE + 2, drawable.widthInt)
	}
}