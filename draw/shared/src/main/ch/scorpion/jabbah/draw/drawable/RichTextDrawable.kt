package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.richtext.RichText
import ch.scorpion.jabbah.base.richtext.RichTextParser
import ch.scorpion.jabbah.base.richtext.StyledChunk
import ch.scorpion.jabbah.base.richtext.TextStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.module.DrawModule
import kotlin.math.abs
import kotlin.math.max

/**
 * Transforms a [RichText] AST to a [RichTextDrawable] that can be drawn
 * with [Graphics2D] operations. Currently supports only single-line text.
 */
class RichTextDrawableTransformer(
	private val richText: RichText,
	private val font: Font
) {

	companion object {
		private const val INDEX_FONT_FACTOR = 0.6
		private const val SUBSCRIPT_OFFSET_FACTOR = 0.2
		private const val SUPERSCRIPT_OFFSET_FACTOR = 0.5
		private const val INDEX_GAP = 2
	}

	private val indexFont = font
		.deriveFont((font.size * INDEX_FONT_FACTOR).toInt())
		.deriveFont(FontStyle.BOLD)

	private val drawable = RichTextDrawable()

	fun transform(): RichTextDrawable {
		var baselineX = 0.0

		richText.children.forEach { fragment ->

			/** Build base text [ChunkView] */
			fragment.text.styledText.chunks.forEach { chunk ->
				addChunkView(chunk, baselineX, 0.0, font).also {
					baselineX += it.width
				}
			}

			/** Build subscript text [ChunkView], if any */
			var subscriptX = baselineX + INDEX_GAP
			fragment.subscript?.styledText?.chunks?.forEach { chunk ->
				addChunkView(chunk, subscriptX, +font.size * SUBSCRIPT_OFFSET_FACTOR, indexFont).also {
					subscriptX += it.width
				}
			}

			/** Build superscript text [ChunkView], if any */
			var superscriptX = baselineX + INDEX_GAP
			fragment.superscript?.styledText?.chunks?.forEach { chunk ->
				addChunkView(chunk, superscriptX, -font.size * SUPERSCRIPT_OFFSET_FACTOR, indexFont).also {
					superscriptX += it.width
				}
			}

			baselineX = max(subscriptX, superscriptX)
		}

		return drawable
	}

	private fun addChunkView(chunk: StyledChunk, baselineX: Double, baselineY: Double, font: Font): ChunkView {
		val chunkView = ChunkView(chunk.text, baselineX, baselineY, font, chunk.style == TextStyle.OVERLINE)
		drawable.addChunkView(chunkView)
		return chunkView
	}
}

class RichTextDrawable : AbstractRectangle() {

	companion object {

		fun of(text: String, font: Font): RichTextDrawable {
			val parser = RichTextParser(text)
			return try {
				RichTextDrawableTransformer(parser.parse(), font).transform()
			} catch (e: SyntaxError) {
				legacy(text, font)
			}
		}

		/**
		 * Creates a simple [RichTextDrawable] for [text] without formatting properties
		 * to be used as fallback for legacy texts that cannot be parsed successfully.
		 */
		private fun legacy(text: String, font: Font): RichTextDrawable =
			RichTextDrawable().apply {
				addChunkView(ChunkView(text, 0.0, 0.0, font))
			}
	}

	/** The coordinates of the [ChunkView]s are relative to the baseline start of the first [ChunkView].*/
	private val chunkViews = mutableListOf<ChunkView>()

	private var overallAscent: Double = 0.0

	/**
	 * The enclosing rectangle of all [ChunkView]s relative to the baseline start, which is (0,0).
	 * This does NOT change when the location of [RichTextDrawable] changes.
	 */
	val baselineRect = Rectangle2D()

	override fun draw(context: DrawContext) {
		context.g.translate(location.x, location.y + overallAscent)
		chunkViews.forEach { it.draw(context.g) }
		context.g.translate(-location.x, -location.y - overallAscent)
	}

	fun draw(g: Graphics2D) {
		g.translate(location.x, location.y + overallAscent)
		chunkViews.forEach { it.draw(g) }
		g.translate(-location.x, -location.y - overallAscent)
	}

	override val lineWidth: Double get() = 1.0

	internal fun addChunkView(chunkView: ChunkView) {
		chunkViews.add(chunkView)

		baselineRect.add(chunkView.x, chunkView.y)
		baselineRect.add(chunkView.x + chunkView.width, chunkView.y + chunkView.height)

		overallAscent = chunkViews.maxOf { abs(it.y) }
		shape.add(chunkView.x, chunkView.y)
		shape.add(chunkView.x + chunkView.width, chunkView.y + chunkView.height)
	}
}

internal class ChunkView(
	private val text: String,
	private val baselineX: Double,
	private val baselineY: Double,
	private val font: Font,
	private val overline: Boolean = false
) {
	companion object {
		private val DEBUG_COLOR = Color.GREEN
		private val DEBUG_STROKE = Stroke(0.5f)
		private val OVERLINE_STROKE = Stroke(1.0f)
	}

	var x: Double = 0.0
		private set

	var y: Double = 0.0
		private set

	var width: Double = 0.0
		private set

	var height: Double = 0.0
		private set

	init {
		val tri = TextRenderInfoFactory.measureSingleLineText(text, font)
		x = baselineX
		y = baselineY - tri.ascent
		width = tri.textBounds.width
		height = tri.textBounds.height
	}

	fun draw(g: Graphics2D) {
		g.font = font

		g.drawString(text, baselineX.toInt(), baselineY.toInt())

		if (overline) {
			g.stroke = OVERLINE_STROKE
			g.drawLine(x, y, x + width, y)
		}

		if (DrawModule.debugGfx) {
			val oldColor = g.color
			g.color = DEBUG_COLOR
			g.stroke = DEBUG_STROKE
			g.drawRect(x, y, width, height)
			g.color = oldColor
		}
	}
}
