package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.TextMeasurer
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.module.DrawModule
import kotlin.math.max

/**
 * Breaks text into multiple lines according to a box with a fix width.
 *
 * Can be aggregated into a [Drawable].
 * Used primarily for rendering multi-line text of the JavaScript platform, but could also
 * be useful on the JVM platform, and therefore contained in the 'shared' module.
 *
 * @property location the upper-left corner of the surrounding box relative to the owner's coordinate system
 */
class MultilineText(
	text: String,
	private val font: Font,
	preferredWidth: Double,
	minWidth: Double = preferredWidth,
	override var location: Point2D = Point2D.ZERO,
	textMeasurer: TextMeasurer = TextRenderInfoFactory
) : AbstractRectangle(), Locatable {

	companion object {
		const val LINE_DIST = 5.0
	}

	private val lines = mutableListOf<String>()

	private val ascent: Int

	private val lineHeight: Double = font.size + LINE_DIST

	init {
		var lAscent = 0
		var maxWidth = 0.0

		for (car in text.split('\n')) {
			var line = ""
			var testWidth = 0.0
			for (word in car.split(' ')) {
				val testLine = "$line$word "
				val textRenderInfo = textMeasurer.measureSingleLineText(testLine, font)
				testWidth = textRenderInfo.textBounds.width
				lAscent = textRenderInfo.ascent.toInt()

				line = if (testWidth > preferredWidth) {
					maxWidth = max(testWidth, maxWidth)
					lines.add(line)
					"$word "
				} else {
					testLine
				}
			}

			maxWidth = max(testWidth, maxWidth)
			lines.add(line)
		}

		ascent = lAscent

		setBounds(location.x, location.y, max(minWidth, maxWidth), lines.size * lineHeight)
	}

	/** ---- [RectangularDrawable] */

	override val lineWidth: Double get() = 0.0

	/** ----- [Drawable] interface */

	override fun draw(context: DrawContext) {
		context.g.font = font
		var yy = location.y + ascent
		for (line in lines) {
			context.g.drawString(line, location.x.toInt(), yy.toInt())
			yy += lineHeight
		}

		DrawModule.drawDebugBoundingBox(this, context.g)
	}

	override fun contains(x: Double, y: Double): Boolean =
		boundingBox.contains(x, y)
}