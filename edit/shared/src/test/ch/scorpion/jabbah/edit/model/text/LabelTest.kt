package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.draw.graphics.FontFamily
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.FontStyle
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [Label].
 */
class LabelTest {

    @Before
    fun setup() {
        EditModuleJvm.require()
    }

    @Test
    fun shouldConstructWithText() {
        val label = Label(text = "Test", font = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, 10))
        assertThat(label.text, `is`("Test"))
    }
}