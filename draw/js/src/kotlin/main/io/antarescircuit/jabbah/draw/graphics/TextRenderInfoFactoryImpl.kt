package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

actual object TextRenderInfoFactoryImpl {

	private val canvas = document.createElement("canvas") as HTMLCanvasElement

	private val context = canvas.getContext("2d") as CanvasRenderingContext2D

	/** The font last applied to [context]; assigning a font parses it, so unchanged fonts aren't assigned again. */
	private var appliedFont: Font? = null

	actual fun measureSingleLineTextImpl(text: String, font: Font): TextRenderInfo {
		if (font != appliedFont) {
			appliedFont = font
			context.font = Graphics2DJs.toJsFont(font)
		}
		var metrics = context.measureText(text)

		// Note: The properties of TextMetrics other than 'width' are not yet supported
		// in most browsers. Therefore we use a heuristic instead.

		val ascent = 7.0 / 8 * font.size
		val descent = 1.0 / 8 * font.size

		return TextRenderInfo(
			Rectangle2D(
				0.0,
				-ascent,
				metrics.width,
				ascent + descent),
			ascent)
	}
}