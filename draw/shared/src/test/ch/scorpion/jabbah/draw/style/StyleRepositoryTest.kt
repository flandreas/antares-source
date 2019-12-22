package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Unit tests for [StyleRepository].
 */
class StyleRepositoryTest {

	@BeforeTest
	fun beforeTest() {
		DrawTestRule.configure()
	}

    val styleType = StyleType("test", "testKey")
    val style: Style = BasicStyle()

    @BeforeTest
    fun setup() {
        BaseModule.require()
        StyleRepository.INSTANCE.clear()
    }

    @Test
    fun shouldRegisterStyleType() {
        StyleRepository.INSTANCE.registerStyleType(styleType)
        assertSame(styleType, StyleRepository.INSTANCE.getStyleType("test"))
    }

    @Test
    fun shouldRegisterStyle() {
        StyleRepository.INSTANCE.registerStyle(styleType, style)
	    assertSame(style, StyleRepository.INSTANCE.getStyle(styleType))
    }

	@Test
	fun shouldReturnStyleTypeInOrderOfRegistration() {
		StyleRepository.INSTANCE.registerStyleType(StyleType("Style A", "testKey"))
		StyleRepository.INSTANCE.registerStyleType(StyleType("Style B", "testKey"))
		StyleRepository.INSTANCE.registerStyleType(StyleType("Style C", "testKey"))

		assertEquals("Style A", StyleRepository.INSTANCE.getStyleTypes()[0].name)
		assertEquals("Style B", StyleRepository.INSTANCE.getStyleTypes()[1].name)
		assertEquals("Style C", StyleRepository.INSTANCE.getStyleTypes()[2].name)
	}
}