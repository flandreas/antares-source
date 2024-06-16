package ch.scorpion.jabbah.draw.style

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock


/**
 * A builder for mocks of [StyleProvider].
 */
class StyleProviderMockBuilder {

    private val styleProvider = mock<StyleProvider>()

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