package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.Rectangle2D

/** Used to implement mocks, because mockk doesn't support mocking object on the js platform.*/
interface TextMeasurer {

	/** Measures rendered single-line, non-HTML text.*/
	fun measureSingleLineText(text: String, font: Font): TextRenderInfo
}

expect object TextRenderInfoFactory : TextMeasurer

/**
 * Represents platform-specific geometrical information about how a text is rendered.
 */
data class TextRenderInfo(val textBounds: Rectangle2D, val ascent: Double)