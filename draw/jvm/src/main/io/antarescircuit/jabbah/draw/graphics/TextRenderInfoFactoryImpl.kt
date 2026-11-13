package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import java.awt.font.FontRenderContext

actual object TextRenderInfoFactoryImpl {

	/** Shared measuring context with identity transform, antialiasing and fractional metrics. Immutable. */
	private val fontRenderContext = FontRenderContext(null, true, true)

	actual fun measureSingleLineTextImpl(text: String, font: Font): TextRenderInfo {
		val awtFont = Graphics2DJvm.toAwtFont(font)
		val rect = awtFont.getStringBounds(text, fontRenderContext)
		val lm = awtFont.getLineMetrics(text, fontRenderContext)
		return TextRenderInfo(Rectangle2D(rect.x, rect.y, rect.width, rect.height), lm.ascent.toDouble())
	}
}
