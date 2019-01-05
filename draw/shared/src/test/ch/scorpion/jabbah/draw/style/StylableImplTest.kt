package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.module.DrawModule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.junit.Assert.*
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
    private val customColor = PredefinedColorRepository.withIdentity(PredefinedColorIdentity.Black)!!
    private val specifiedStyle = BasicStyle(color = styleColor)

    @Test
    fun shouldUseStyleForegroundColor() {
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, specifiedStyle)
        val stylable = StylableImpl(styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)
        assertThat(stylable.foregroundColor, `is`(sameInstance(specifiedStyle.color.foregroundColor)))
    }

    @Test
    fun shouldUseCustomForegroundColor() {
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, specifiedStyle)
        val stylable = StylableImpl(customColor = customColor, styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)
        assertThat(stylable.foregroundColor, `is`(sameInstance(customColor.color.foregroundColor)))
    }

    @Test
    fun shouldUseStyleColor() {
        val propertyColor = Color(1, 2, 3)
        DrawModule.properties.set(Style.PROP_FOREGROUND_COLOR, propertyColor)
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, BasicStyle())
        val stylable = StylableImpl(styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)
        assertThat(stylable.foregroundColor, `is`(sameInstance(propertyColor)))
    }

	@Test
	fun shouldDeactivateShadow() {
		val style = BasicStyle(shadow = true)
		StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, style)
		val stylable = StylableImpl(styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)

		stylable.customShadow = false

		assertThat(stylable.shadow, `is`(false))
	}
}