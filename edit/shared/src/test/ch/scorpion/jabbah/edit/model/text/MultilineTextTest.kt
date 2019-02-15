package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.drawable.MultilineText
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.TextRenderInfo
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [MultilineText].
 */
class MultilineTextTest {

    @Test
    fun shouldWrapEveryWord() {
        val text = MultilineText("This is a test", FontImpl(size = 10), 15.0, textRenderInfoFactory = textRenderInfoFactory())
        assertEquals(4 * (10 + MultilineText.LINE_DIST), text.height)
    }

    @Test
    fun shouldWrapEverySecondWord() {
        val text = MultilineText("This is a test", FontImpl(size = 10), 25.0, textRenderInfoFactory = textRenderInfoFactory())
        assertEquals(2 * (10 + MultilineText.LINE_DIST), text.height)
    }

    private fun textRenderInfoFactory(): TextRenderInfoFactory {
        val textRenderInfoFactory = mockk<TextRenderInfoFactory>()

	    val slot = slot<String>()
	    every {
		    textRenderInfoFactory.measureSingleLineText(text = capture(slot), font = any())
	    } answers {
		    TextRenderInfo(Rectangle2D(0, 0, 10 * StringUtils.countChar(slot.captured, ' '), 0), 8.0)
	    }

        return textRenderInfoFactory
    }
}