package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.module.DrawModule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [StylableImpl].
 */
class StylableImplTest {

    companion object {

        @ClassRule @JvmField
        val drawTestRule = DrawTestRule()
    }


    private val styleColor = CompositeColor()
    private val customColor = PredefinedColor("test", "testKey", CompositeColor())
    private val specifiedStyle = BasicStyle(color = styleColor)

    @Test
    fun shouldUseStyleForegroundColor() {
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, specifiedStyle)
        val stylable = StylableImpl(styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)
        assertThat(stylable.foregroundColor, `is`(`sameInstance`(specifiedStyle.color.foregroundColor)))
    }

    @Test
    fun shouldUseCustomForegroundColor() {
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, specifiedStyle)
        val stylable = StylableImpl(customColor = customColor, styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)
        assertThat(stylable.foregroundColor, `is`(`sameInstance`(customColor.color.foregroundColor)))
    }

    @Test
    fun shouldUseStyleColor() {
        val propertyColor = Color(1, 2, 3)
        DrawModule.properties.set(Style.PROP_FOREGROUND_COLOR, propertyColor)
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, BasicStyle())
        val stylable = StylableImpl(styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)
        assertThat(stylable.foregroundColor, `is`(`sameInstance`(propertyColor)))
    }
}