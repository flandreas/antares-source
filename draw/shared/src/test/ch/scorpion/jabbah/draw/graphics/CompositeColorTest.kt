package ch.scorpion.jabbah.draw.graphics

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test

/** Unit tests for [CompositeColor].*/
class CompositeColorTest {

    @Test
    fun shouldCalculateDisableTextColor() {
        assertThat(CompositeColor(backgroundColor = Color.BLACK, textColor = Color.WHITE).disabledTextColor, `is`(Color(128, 128, 128)))
        assertThat(CompositeColor(backgroundColor = Color.WHITE, textColor = Color.BLACK).disabledTextColor, `is`(Color(128, 128, 128)))
    }

}