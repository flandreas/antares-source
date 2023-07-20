package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.Rectangle2D
import java.awt.font.FontRenderContext

actual object TextRenderInfoFactory : TextMeasurer {

	override fun measureSingleLineText(text: String, font: Font): TextRenderInfo {
		val awtFont = java.awt.Font(font.family.fontName, Graphics2DJvm.fromFontStyle(font), font.size)
		val context = FontRenderContext(awtFont.transform, true, false)
		val rect = awtFont.getStringBounds(text, context)
		val lm = awtFont.getLineMetrics(text, context)
		return TextRenderInfo(Rectangle2D(rect.x, rect.y, rect.width, rect.height), lm.ascent.toDouble())
	}
}