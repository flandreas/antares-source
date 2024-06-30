package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.parser.TextLocation.Companion.UNDEFINED
import ch.scorpion.jabbah.base.richtext.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.module.DrawModule
import kotlin.math.abs
import kotlin.math.max

/**
 * Displays a [RichText] AST using [Graphics2D] operations as single-line text.
 */
class RichTextDrawable(
	richText: RichText,
	private val baseFont: Font
) : AbstractRectangle() {

	companion object {

		private val LOG by logger(RichTextDrawable::class)

		private const val INDEX_FONT_FACTOR = 0.7
		private const val SUBSCRIPT_OFFSET_FACTOR = 0.2
		private const val SUPERSCRIPT_OFFSET_FACTOR = 0.5
		private const val INDEX_GAP = 2
		private val UNDERLINE_STROKE = Stroke(1.0f)

		private val DEBUG_COLOR = Color.GREEN
		private val DEBUG_STROKE = Stroke(0.5f)
		private val OVERLINE_STROKE = Stroke(1.0f)

		/** The distance between lines in multiline text.*/
		const val LINE_DIST = 3.0

		/**
		 * If `true`, [SyntaxError]s while parsing are caught, and the raw text is displayed without
		 * interpreting styles. This is needed for backward compatibility with texts created before introduction
		 * of the new [RichText] format, e.g. text that contains parens.
		 * Can be temporarily set to `false` when editing new texts to let the [SyntaxError] propagate
		 * to higher system layers, where it can be presented to the user.
		 */
		var LEGACY_MODE: Boolean = true

		/**
		 * Parses the formatted plain text as [RichText] and creates a [RichTextDrawable]
		 * that can render it for the specified [font].
		 */
		fun of(text: String, font: Font, textMeasurer: TextMeasurer = TextRenderInfoFactory): RichTextDrawable =
			try {
				transformToSingleLine(RichTextParser(text).parse(), font, textMeasurer)
			} catch (e: SyntaxError) {
				if (LEGACY_MODE) {
					transformToSingleLine(legacyRichText(text, e), font, textMeasurer)
				} else {
					throw e
				}
			}

		/**
		 * Treats the text as-is and creates a [RichTextDrawable] that ignores all markup.
		 */
		fun asPlain(text: String, font: Font, textMeasurer: TextMeasurer = TextRenderInfoFactory) =
			transformToSingleLine(RichText.asPlain(text), font, textMeasurer)

		fun multiline(text: String, font: Font, preferredWidth: Double, textMeasurer: TextMeasurer = TextRenderInfoFactory): RichTextDrawable =
			try {
				transformToMultiline(RichTextParser(text).parse(), font, preferredWidth, textMeasurer)
			} catch (e: SyntaxError) {
				if (LEGACY_MODE) {
					transformToMultiline(legacyRichText(text, e), font, preferredWidth, textMeasurer)
				} else {
					throw e
				}
			}

		/**
		 * Creates a simple [RichText] for [text] without formatting properties
		 * to be used as fallback for legacy texts that cannot be parsed successfully.
		 */
		private fun legacyRichText(text: String, error: SyntaxError): RichText {
			LOG.debug("Resorting to legacy format: ${error.message} at ${error.location}")
			return RichText.asPlain(text)
		}

		/**
		 * Transforms a [RichText] AST to a [RichTextDrawable] that can be drawn
		 * with [Graphics2D] operations. Currently, supports only single-line text.
		 */
		private fun transformToSingleLine(richText: RichText, font: Font, textMeasurer: TextMeasurer): RichTextDrawable {
			val drawable = RichTextDrawable(richText, font)
			var baselineX = 0.0

			richText.children.forEach { fragment ->

				/** Build base text [ChunkView] */
				fragment.text.styledText.chunks.forEach { chunk ->
					drawable.createAndAddChunkView(chunk.text, chunk.style, baselineX, 0.0, false, textMeasurer).also {
						baselineX += it.width
					}
				}

				/** Build subscript text [ChunkView], if any */
				var subscriptX = baselineX + INDEX_GAP
				fragment.subscript?.styledText?.chunks?.forEach { chunk ->
					drawable.createAndAddChunkView(chunk.text, chunk.style, subscriptX, +font.size * SUBSCRIPT_OFFSET_FACTOR, true, textMeasurer).also {
						subscriptX += it.width
					}
				}

				/** Build superscript text [ChunkView], if any */
				var superscriptX = baselineX + INDEX_GAP
				fragment.superscript?.styledText?.chunks?.forEach { chunk ->
					drawable.createAndAddChunkView(chunk.text, chunk.style, superscriptX, -font.size * SUPERSCRIPT_OFFSET_FACTOR, true, textMeasurer).also {
						superscriptX += it.width
					}
				}

				baselineX = max(subscriptX, superscriptX)
			}

			return drawable
		}

		private fun transformToMultiline(richText: RichText, font: Font, preferredWidth: Double, textMeasurer: TextMeasurer): RichTextDrawable {
			val drawable = RichTextDrawable(richText, font)
			var baselineX = 0.0
			var baselineY = 0.0
			var lineWidth = 0.0

			richText.children.forEach { fragment ->

				/** Build base text [ChunkView] */
				fragment.text.styledText.chunks
					.flatMap { it.splitWords() }
					.forEach { chunk ->
						val lines = chunk.text.split('\n')

						lines.forEachIndexed { index, line ->

							val wordView = drawable.createChunkView(
								line,
								chunk.style,
								baselineX,
								baselineY,
								indexed = false,
								textMeasurer
							)
							val textWidth = wordView.width

							if (lineWidth + textWidth > preferredWidth) {
								// No room for word in current line
								if (baselineX > 0.0) {
									// Terminate current line, place word in next line
									wordView.baselineX = 0.0
									wordView.baselineY += wordView.height + LINE_DIST
									drawable.addChunkView(wordView)

									baselineX = wordView.width
									baselineY = wordView.baselineY
									lineWidth = wordView.width
								} else {
									// single overly long word, but place anyway in current line (no hyphenation)
									wordView.baselineX = 0.0
									wordView.baselineY = baselineY
									drawable.addChunkView(wordView)

									baselineX = 0.0
									baselineY += wordView.height + LINE_DIST
									lineWidth = 0.0
								}
							} else {
								// Still room for word in current line
								wordView.baselineX = baselineX
								wordView.baselineY = baselineY
								drawable.addChunkView(wordView)

								baselineX += textWidth
								lineWidth += textWidth
							}

							if (index < lines.size - 1) {
								// Add new line
								baselineX = 0.0
								baselineY += wordView.height + LINE_DIST
								lineWidth = 0.0
							}
						}
					}

				/** Build subscript text [ChunkView], if any.*/
				var subscriptX = baselineX + INDEX_GAP
				fragment.subscript?.styledText?.chunks?.forEach { chunk ->
					drawable.createAndAddChunkView(chunk.text, chunk.style, subscriptX, +font.size * SUBSCRIPT_OFFSET_FACTOR, true, textMeasurer).also {
						subscriptX += it.width
					}
				}

				/** Build superscript text [ChunkView], if any. */
				var superscriptX = baselineX + INDEX_GAP
				fragment.superscript?.styledText?.chunks?.forEach { chunk ->
					drawable.createAndAddChunkView(chunk.text, chunk.style, superscriptX, -font.size * SUPERSCRIPT_OFFSET_FACTOR, true, textMeasurer).also {
						superscriptX += it.width
					}
				}

				baselineX = max(subscriptX, superscriptX)
				lineWidth = baselineX
			}

			return drawable
		}
	}

	/** The coordinates of the [ChunkView]s are relative to the baseline start of the first [ChunkView].*/
	private val chunkViews = mutableListOf<ChunkView>()

	private val boldFont: Font by lazy { baseFont.deriveFont(FontStyle.BOLD) }

	private val italicFont: Font by lazy { baseFont.deriveFont(FontStyle.ITALIC) }

	private val boldItalicFont: Font by lazy { baseFont.deriveFont(FontStyle.BOLD).deriveFont(FontStyle.ITALIC) }

	private val indexFont: Font by lazy {
		baseFont
			.deriveFont((baseFont.size * INDEX_FONT_FACTOR).toInt())
			.deriveFont(FontStyle.BOLD)
	}

	private val italicIndexFont: Font by lazy {
		baseFont
			.deriveFont((baseFont.size * INDEX_FONT_FACTOR).toInt())
			.deriveFont(FontStyle.BOLD)
			.deriveFont(FontStyle.ITALIC)
	}

	/**
	 * The enclosing rectangle of all [ChunkView]s relative to the baseline start, which is (0,0).
	 * This does NOT change when the location of [RichTextDrawable] changes.
	 */
	val baselineRect = Rectangle2D()

	var underline: Boolean = false

	private val maxOverlineLevel = richText.getMaxOverlineLevel()

	override fun draw(context: DrawContext) {
		draw(context.g)
	}

	fun draw(g: Graphics2D) {
		val ascent = abs(baselineRect.y)
		g.translate(location.x, location.y + ascent)

		chunkViews.forEach { it.draw(g, maxOverlineLevel) }
		if (underline) {
			val y = -ascent.toInt() + heightInt + 1
			g.stroke = UNDERLINE_STROKE
			g.drawLine(0, y, widthInt, y)
		}
		g.translate(-location.x, -location.y - ascent)
	}

	override val lineWidth: Double get() = 1.0

	private fun createChunkView(text: String, style: TextStyle, baselineX: Double, baselineY: Double, indexed: Boolean, textMeasurer: TextMeasurer): ChunkView =
		ChunkView(text, style, baselineX, baselineY, indexed = indexed, textMeasurer)

	private fun createAndAddChunkView(text: String, style: TextStyle, baselineX: Double, baselineY: Double, indexed: Boolean, textMeasurer: TextMeasurer): ChunkView =
		createChunkView(text, style, baselineX, baselineY, indexed, textMeasurer).also {
			addChunkView(it)
		}

	private fun addChunkView(chunkView: ChunkView) {
		chunkViews.add(chunkView)

		baselineRect.add(chunkView.x, chunkView.y)
		baselineRect.add(chunkView.x + chunkView.width, chunkView.y + chunkView.height)

		shape.add(chunkView.x, chunkView.y)
		shape.add(chunkView.x + chunkView.width, chunkView.y + chunkView.height)
	}

	private inner class ChunkView(
		private val text: String,
		private val style: TextStyle,
		baselineX: Double,
		baselineY: Double,
		private val indexed: Boolean = false,
		textMeasurer: TextMeasurer
	) {
		var x: Double = 0.0
			private set

		var y: Double = 0.0
			private set

		var width: Double = 0.0
			private set

		var height: Double = 0.0
			private set

		var baselineX: Double = baselineX
			set(value) {
				field = value
				x = baselineX
			}

		var baselineY: Double = baselineY
			set(value) {
				field = value
				y = field - ascent
			}

		private var ascent: Double = 0.0

		private val localFont: Font get() =
			if (indexed) {
				if (style.italic) italicIndexFont else indexFont
			} else {
				if (style.bold) {
					if (style.italic) boldItalicFont else boldFont
				} else {
					if (style.italic) italicFont else baseFont
				}
			}

		init {
			val tri = textMeasurer.measureSingleLineText(text, localFont)
			ascent = tri.ascent
			x = baselineX
			y = baselineY - ascent
			width = tri.textBounds.width
			height = tri.textBounds.height
		}

		fun draw(g: Graphics2D, maxOverlineLevel: Int) {

			g.font = localFont

			g.drawString(text, baselineX.toInt(), baselineY.toInt())

			for (level in 1 .. style.overlineLevel) {
				g.stroke = OVERLINE_STROKE
				val lineY = y - 2.5 * ( maxOverlineLevel - level)
				if (style.italic) {
					g.drawLine(x + 2, lineY, x + width, lineY)
				} else {
					g.drawLine(x, lineY, x + width, lineY)
				}
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
}
