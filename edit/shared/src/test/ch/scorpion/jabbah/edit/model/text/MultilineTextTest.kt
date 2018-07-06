package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.drawable.MultilineText
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.TextRenderInfo
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import com.nhaarman.mockitokotlin2.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [MultilineText].
 */
class MultilineTextTest {

    @Test
    fun shouldWrapEveryWord() {
        val text = MultilineText("This is a test", FontImpl(size = 10), 15.0, textRenderInfoFactory = textRenderInfoFactory())
        assertThat(text.height, `is`(4 * (10 + MultilineText.LINE_DIST)))
    }

    @Test
    fun shouldWrapEverySecondWord() {
        val text = MultilineText("This is a test", FontImpl(size = 10), 25.0, textRenderInfoFactory = textRenderInfoFactory())
        assertThat(text.height, `is`(2 * (10 + MultilineText.LINE_DIST)))
    }

    private fun textRenderInfoFactory(): TextRenderInfoFactory {
        val textRenderInfoFactory = mock<TextRenderInfoFactory>()
        whenever(textRenderInfoFactory.measureSingleLineText(any(), any())).thenAnswer{
            // Pretend that every word is 10px wide
            TextRenderInfo(Rectangle2D(0, 0, 10 * StringUtils.countChar(it.arguments[0] as String, ' '), 0), 8.0)
        }

        return textRenderInfoFactory
    }
}