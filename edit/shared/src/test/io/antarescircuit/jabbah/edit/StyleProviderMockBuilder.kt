package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.draw.style.BasicStyle
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

/**
 * A builder for mocks of [StyleProvider].
 * TODO: Copy/Paste of corresponding class in test package of io.antarescircuit.jabba.draw, because found no
 * way yet to reuse with gradle.
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