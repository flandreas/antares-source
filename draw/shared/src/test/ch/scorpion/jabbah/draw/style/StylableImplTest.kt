package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule.RED
import ch.scorpion.jabbah.draw.module.DrawModule
import kotlin.test.*

class StylableImplTest {

    private val styleColor: CompositeColor
    private val customColor: PredefinedColor
    private val specifiedStyle: BasicStyle

    init {
        DrawTestRule.configure()
        styleColor = CompositeColor()
        customColor = PredefinedColorRepository.withIdentity(PredefinedColorIdentity.Black)!!
        specifiedStyle = BasicStyle(color = styleColor)
    }

    @Test
    fun shouldUseStyleForegroundColor() {
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, specifiedStyle)
        val stylable = StylableImpl(styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)
        assertSame(stylable.foregroundColor, specifiedStyle.color.foregroundColor)
    }

    @Test
    fun shouldUseCustomForegroundColor() {
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, specifiedStyle)
        val stylable = StylableImpl(customColor = customColor, styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)
	    assertSame(stylable.foregroundColor, customColor.color.foregroundColor)
    }

    @Test
    fun shouldUseStyleColor() {
        val propertyColor = Color(1, 2, 3)
        DrawModule.properties.set(Style.PROP_FOREGROUND_COLOR, propertyColor)
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, BasicStyle())
        val stylable = StylableImpl(styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)
	    assertSame(stylable.foregroundColor, propertyColor)
    }

	@Test
	fun shouldDeactivateShadow() {
		val style = BasicStyle(shadow = true)
		StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, style)
		val stylable = StylableImpl(styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)

		stylable.customShadow = false

		assertFalse(stylable.shadow)
	}

    @Test
    fun shouldAvoidUnnecessaryCompositeColorInstantiation() {
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, specifiedStyle)
        val stylable = StylableImpl(customColor = null, styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)

        val color1 = stylable.color
        val color2 = stylable.color

        assertSame(color1, color2)
    }

    @Test
    fun shouldUpdateColorForCustomColor() {
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, specifiedStyle)
        PredefinedColorRepository.register(PredefinedColor(PredefinedColorIdentity.Red, RED))
        val stylable = StylableImpl(customColor = null, styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)

        val color1 = stylable.color
        stylable.customColor = PredefinedColorRepository.withIdentity(PredefinedColorIdentity.Red)
        val color2 = stylable.color

        assertNotSame(color1, color2)
        assertEquals(stylable.customColor?.color?.foregroundColor, color2.foregroundColor)
    }

    @Test
    fun shouldUpdateColorForStyle() {
        StyleRepository.INSTANCE.registerStyle(StyleType.FIGURE, specifiedStyle)
        val stylable = StylableImpl(customColor = null, styleProvider = StyleRepository.INSTANCE, styleType = StyleType.FIGURE)

        val color1 = stylable.color
        stylable.styleType = StyleType.BACKGROUND
        val color2 = stylable.color

        assertNotSame(color1, color2)
    }
}