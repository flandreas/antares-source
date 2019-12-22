package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document

actual object TextRenderInfoFactory : TextMeasurer {

	private val LOG by logger(TextRenderInfoFactory::class)

	private val canvas = document.createElement("canvas") as HTMLCanvasElement

	override fun measureSingleLineText(text: String, font: Font): TextRenderInfo {
		val context = canvas.getContext("2d") as CanvasRenderingContext2D
		context.font = Graphics2DJs.toJsFont(font)
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

	override fun measureHtmlText(text: String, font: Font, width: Int): TextRenderInfo {
		LOG.error("TextRenderInfoFactoryJS.measureHtmlText not yet implemented")
		throw UnsupportedOperationException("not implemented")
	}
}