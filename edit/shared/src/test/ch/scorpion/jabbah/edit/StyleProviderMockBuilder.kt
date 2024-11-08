package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

/**
 * A builder for mocks of [StyleProvider].
 * TODO: Copy/Paste of corresponding class in test package of ch.scorpion.jabba.draw, because found no
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