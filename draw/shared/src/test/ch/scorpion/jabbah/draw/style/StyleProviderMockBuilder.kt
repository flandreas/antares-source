package ch.scorpion.jabbah.draw.style

import io.mockk.every
import io.mockk.mockk

/**
 * A builder for mocks of [StyleProvider].
 */
class StyleProviderMockBuilder {

    private val styleProvider = mockk<StyleProvider>()

    init {
	    every { styleProvider.getStyleType(any()) } returns StyleType.FIGURE
	    every { styleProvider.getStyle(any()) } returns BasicStyle()
    }

    fun withStyleType(name: String, styleType: StyleType): StyleProviderMockBuilder {
	    every { styleProvider.getStyleType(name) } returns styleType
        return this
    }

    fun build() = styleProvider
}