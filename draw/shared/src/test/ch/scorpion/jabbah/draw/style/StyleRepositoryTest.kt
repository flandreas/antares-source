package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.DrawTestRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.`sameInstance`
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [StyleRepository].
 */
class StyleRepositoryTest {

	companion object {
		@ClassRule
		@JvmField
		val drawTestRule = DrawTestRule()
	}

    val styleType = StyleType("test", "testKey")
    val style: Style = BasicStyle()

    @Before
    fun setup() {
        BaseModuleJvm.require()
        StyleRepository.INSTANCE.clear()
    }

    @Test
    fun shouldRegisterStyleType() {
        StyleRepository.INSTANCE.registerStyleType(styleType)
        assertThat(StyleRepository.INSTANCE.getStyleType("test"), `is`(`sameInstance`(styleType)))
    }

    @Test
    fun shouldRegisterStyle() {
        StyleRepository.INSTANCE.registerStyle(styleType, style)
        assertThat(StyleRepository.INSTANCE.getStyle(styleType), `is`(`sameInstance`(style)))
    }

	@Test
	fun shouldReturnStyleTypeInOrderOfRegistration() {
		StyleRepository.INSTANCE.registerStyleType(StyleType("Style A", "testKey"))
		StyleRepository.INSTANCE.registerStyleType(StyleType("Style B", "testKey"))
		StyleRepository.INSTANCE.registerStyleType(StyleType("Style C", "testKey"))

		assertThat(StyleRepository.INSTANCE.getStyleTypes()[0].name, `is`("Style A"))
		assertThat(StyleRepository.INSTANCE.getStyleTypes()[1].name, `is`("Style B"))
		assertThat(StyleRepository.INSTANCE.getStyleTypes()[2].name, `is`("Style C"))
	}
}