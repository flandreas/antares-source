package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.`sameInstance`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [StyleRepository].
 */
class StyleRepositoryTest {

    val styleType = StyleType("test", "testkey")
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
}