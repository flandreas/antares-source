package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.base.geom.Rectangle2D

/**
 * Used to implement mocks, because mockk doesn't support mocking object on the js platform,
 * and mokkery doesn't support mocking Kotlin objects.
 * */
interface TextMeasurer {

	/** Measures rendered single-line, non-HTML text.*/
	fun measureSingleLineText(text: String, font: Font): TextRenderInfo
}

object TextRenderInfoFactory : TextMeasurer {
	override fun measureSingleLineText(text: String, font: Font): TextRenderInfo {
		return TextRenderInfoFactoryImpl.measureSingleLineTextImpl(text, font)
	}
}

/**
 * Since Kotlin 2.0.0, expected object can't extend interfaces anymore.
 * That's the reason why [TextRenderInfoFactory] (which implements the interface necessary for mocking)
 * and [TextRenderInfoFactoryImpl] (which can be compiled with Kotlin 2.0.0) are separate.
 */
expect object TextRenderInfoFactoryImpl {
	fun measureSingleLineTextImpl(text: String, font: Font): TextRenderInfo
}

/**
 * Represents platform-specific geometrical information about how a text is rendered.
 */
data class TextRenderInfo(val textBounds: Rectangle2D, val ascent: Double)