package io.antarescircuit.jabbah.edit.model.text

import io.antarescircuit.jabbah.draw.graphics.FontImpl
import io.antarescircuit.jabbah.draw.graphics.LogicalFontFamily
import io.antarescircuit.jabbah.draw.graphics.FontStyle
import io.antarescircuit.jabbah.edit.EditTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [Label].
 */
class LabelTest {

	@BeforeTest
	fun setup() {
		EditTestRule.configure()
	}

    @Test
    fun shouldConstructWithText() {
        val label = Label(text = "Test", font = FontImpl(LogicalFontFamily.SANS_SERIF, FontStyle.PLAIN.value, 10))
        assertEquals("Test", label.text)
    }

    @Test
    fun shouldConstructWithPlainText() {
        // essentially, check that the constructor doesn't throw - which it would if it parsed the text as RichText
        val label = Label(
            text = "!",
            font = FontImpl(LogicalFontFamily.MONOSPACED, FontStyle.PLAIN.value, 10),
            richText = false)
        assertEquals("!", label.text)
    }
}