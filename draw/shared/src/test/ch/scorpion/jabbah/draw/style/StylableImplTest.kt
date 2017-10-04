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

        val propertyColor = Color(0, 0, 0)

        @BeforeClass @JvmStatic
        fun setup() {
            DrawModule.properties.set(Style.PROP_FOREGROUND_COLOR, propertyColor)
        }
    }


    val styleColor = CompositeColor()
    val customColor = PredefinedColor("test", "testKey", CompositeColor())
    val unspecifiedStyle = BasicStyle()
    val specifiedStyle = BasicStyle(color = styleColor)

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
    fun shouldUsePropertyColor() {
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, unspecifiedStyle)
        val stylable = StylableImpl(styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)
        assertThat(stylable.foregroundColor, `is`(`sameInstance`(propertyColor)))
    }
}