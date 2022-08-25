package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.LogicalFontFamily
import ch.scorpion.jabbah.draw.graphics.FontStyle
import ch.scorpion.jabbah.edit.EditTestRule
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
}