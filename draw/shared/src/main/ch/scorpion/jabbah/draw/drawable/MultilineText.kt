package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.text.StyledText
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.module.DrawModule
import kotlin.math.max

/**
 * Breaks text into multiple lines according to a box with a fix width.
 *
 * Can be aggregated into a [Drawable].
 * Used primarily for rendering multi-line text of the JavaScript platform, but could also
 * be useful on the JVM platform, and therefore contained in the 'shared' module.
 *
 * Attempts to render according to the style information in [StyledText]. Besides standard (normal) text,
 * currently only bold is supported.
 *
 * @property location the upper-left corner of the surrounding box relative to the owner's coordinate system
 */
class MultilineText(
	text: StyledText,
	private val font: Font,
	preferredWidth: Double,
	minWidth: Double = preferredWidth,
	override var location: Point2D = Point2D.ZERO,
	textMeasurer: TextMeasurer = TextRenderInfoFactory
) : AbstractRectangle(), Locatable {

	companion object {
		const val LINE_DIST = 5.0
	}

	private val boldFont = font.deriveFont(FontStyle.BOLD)

	private val ascent: Int

	private val lineHeight: Double = font.size + LINE_DIST

	private val words = mutableListOf<Word>()

	private data class Word(val text: String, val pos: Point2D, val font: Font)

	init {
		var lAscent = 0
		var maxWidth = 0.0
		var x: Double
		var y = font.size.toDouble() - lineHeight

		val blankWidth = textMeasurer.measureSingleLineText(" ", font).textBounds.width
		for (car in text.splitLines()) {
			var lineWidth = 0.0
			x = location.x
			y += lineHeight
			for (word in car.split( ' ')) {
				// Word splitting swallows the blanks

				val wordFont = styledFont(word)

				val wordRenderInfo = textMeasurer.measureSingleLineText(word.asPlainText(), wordFont)
				val wordWidth = wordRenderInfo.textBounds.width
				lAscent = wordRenderInfo.ascent.toInt()
				val textWidth = if (x != 0.0) blankWidth + wordWidth else wordWidth

				if (lineWidth + textWidth > preferredWidth) {
					// No room for word in current line
					if (x != 0.0) {
						// Terminate current line, place word in next line
						maxWidth = max(lineWidth, maxWidth)
						x = 0.0
						y += lineHeight
						words.add(Word(word.asPlainText(), Point2D(x, y), wordFont))
						lineWidth = wordWidth
						x += wordWidth
					} else {
						// single overly long word, but place anyway in current line (no hyphenation)
						maxWidth = max(wordWidth, maxWidth)
						words.add(Word(word.asPlainText(), Point2D(x, y), wordFont))
						lineWidth = 0.0
						y += lineHeight
					}
				} else {
					// Still room for word in current line
					val wordText = if (x == 0.0) word.asPlainText() else " ${word.asPlainText()}"
					val wordTextWidth = if (x == 0.0) wordWidth else wordWidth + blankWidth
					words.add(Word(wordText, Point2D(x, y), wordFont))
					x += wordTextWidth
					lineWidth += wordTextWidth
				}
			}

			maxWidth = max(lineWidth, maxWidth)
		}

		ascent = lAscent
		setBounds(location.x, location.y, max(minWidth, maxWidth), y + LINE_DIST)
	}

	private fun styledFont(text: StyledText): Font = if (text.isBold(0)) boldFont else font

	/** ---- [RectangularDrawable] */

	override val lineWidth: Double get() = 0.0

	/** ----- [Drawable] interface */

	override fun draw(context: DrawContext) {
		draw(context.g)
		DrawModule.drawDebugBoundingBox(this, context.g)
	}

	override fun contains(x: Double, y: Double): Boolean =
		boundingBox.contains(x, y)

	/** ---- [MultilineText] */

	fun draw(g: Graphics2D) {
		g.font = font
		g.translate(location.x, location.y)
		for (word in words) {
			g.font = word.font
			g.drawString(word.text, word.pos.xInt, word.pos.yInt)
		}
		g.translate(-location.x, -location.y)
	}
}