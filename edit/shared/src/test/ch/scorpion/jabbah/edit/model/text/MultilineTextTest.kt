package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.text.StyledTextBuilder
import ch.scorpion.jabbah.draw.drawable.MultilineText
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.TextMeasurer
import ch.scorpion.jabbah.draw.graphics.TextRenderInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [MultilineText].
 */
class MultilineTextTest {

	companion object {
		private const val FONT_SIZE = 10
		private val textMeasurer = textMeasurer()
		private val font = FontImpl(size = FONT_SIZE)

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

    @Test
    fun shouldWrapEveryWord() {
        val text = MultilineText(StyledTextBuilder().append("This is a test").build(), font, 2.0 * FONT_SIZE, textMeasurer = textMeasurer)
        assertEquals(4 * (FONT_SIZE + MultilineText.LINE_DIST), text.height)
	    assertEquals(4 * FONT_SIZE, text.widthInt)
    }

    @Test
    fun shouldWrapEverySecondWord() {
        val text = MultilineText(StyledTextBuilder().append("This is a test").build(), font, 7.0 * FONT_SIZE, textMeasurer = textMeasurer)
        assertEquals(2 * (FONT_SIZE + MultilineText.LINE_DIST), text.height)
	    assertEquals(7 * FONT_SIZE, text.widthInt)
    }

	@Test
	fun shouldCalculateWidth() {
		val text = MultilineText(StyledTextBuilder().append("0123456789 0123456789").build(), font, 15.0 * FONT_SIZE, minWidth = 5.0 * FONT_SIZE, textMeasurer = textMeasurer)
		assertEquals(10 * FONT_SIZE, text.widthInt)
	}

	@Test
	fun shouldApplyStyles() {
		val text = MultilineText(
			StyledTextBuilder()
				.appendBold("Bold:")
				.append(" This is a normal text.")
				.build(),
			font,
			15.0 * FONT_SIZE,
			textMeasurer = textMeasurer)

		assertEquals(15 * FONT_SIZE, text.widthInt)
	}

	@Test
	fun shouldBreakAtNewline() {
		val text = MultilineText(
			StyledTextBuilder().append("Line1\nLine2").build(),
			font, 15.0 * FONT_SIZE, textMeasurer = textMeasurer)
		assertEquals(2 * (FONT_SIZE + MultilineText.LINE_DIST), text.height)
	}

	@Test
	fun shouldRenderEmptyLine() {
		val text = MultilineText(
			StyledTextBuilder().append("Line1\n\nLine2").build(),
			font, 15.0 * FONT_SIZE, textMeasurer = textMeasurer)
		assertEquals(3 * (FONT_SIZE + MultilineText.LINE_DIST), text.height)
	}
}