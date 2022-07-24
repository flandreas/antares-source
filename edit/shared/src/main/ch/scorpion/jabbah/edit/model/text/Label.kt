package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.text.FormattedText
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Implemented by classes having a [Label].
 * Primarily used for forwarding changes of [Rotation] to [Label]s needing to react.
 */
interface Labeled {
	val label: Label
}

/**
 * A [Drawable] that displays a simple, single line text.
 *
 * @property text the original text
 * @property font The [Font] in which this [Label] is rendered
 * @property location The location at which this [Label] is rendered. The interpretation of this location depends on the
 *      horizontal and vertical orientation.
 */
class Label(
	text: String?,
	font: Font,
	var color: Color? = null,
	horizontalAlignment: HorizontalAlignment = DEFAULT_HORIZONTAL_ALIGNMENT,
	verticalAlignment: VerticalAlignment = DEFAULT_VERTICAL_ALIGNMENT,
	location: Point2D = Point2D.ZERO,
	rotationDisplayStrategy: RotationDisplayStrategy = RotationDisplayStrategy.IGNORE,
	val rotation: Rotation = Rotation.R0,
	ownerRotation: Rotation = Rotation.R0
) : AbstractDrawable() {

	companion object {
		private val DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.CENTER
		private val DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.CENTER
		private const val BOUNDS_INSET = 1

		private val OVERLINE_STROKE = Stroke()
	}

	var text: String = text ?: ""
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				displayableText = calculateDisplayableText()
				updateGeometry()
			}
		}

	var font: Font = font
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateGeometry()
			}
		}

	var location: Point2D = location
		get() = Point2D(field)
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateGeometry()
			}
		}

	private var _horizontalAlignment: HorizontalAlignment = horizontalAlignment
	var horizontalAlignment: HorizontalAlignment
		get() = _horizontalAlignment
		set(value) {
			if (_horizontalAlignment != value) {
				invalidate()
				_horizontalAlignment = value
				updateGeometry()
			}
		}

	private var _verticalAlignment: VerticalAlignment = verticalAlignment
	var verticalAlignment: VerticalAlignment
		get() = _verticalAlignment
		set(value) {
			if (verticalAlignment != value) {
				invalidate()
				_verticalAlignment = value
				updateGeometry()
			}
		}

	var alignment: Alignment
		get() = Alignment(horizontalAlignment, verticalAlignment)
		set(value) {
			if (alignment != value) {
				invalidate()
				_horizontalAlignment = value.horizontal
				_verticalAlignment = value.vertical
				updateGeometry()
			}
		}

	var rotationDisplayStrategy: RotationDisplayStrategy = rotationDisplayStrategy
		set(value) {
			if (field != value) {
				field = value
				invalidate()
			}
		}

	/** Only used for un-rotating the drawn text if the rotation angle is 180 degrees */
	var ownerRotation: Rotation = ownerRotation

	/** The displayable text after conversion of negated representation. */
	private var displayableText = FormattedText.empty()

	/** The [Rectangle2D] that contains the text entirely.*/
	val bounds = Rectangle2D()

	/** The point at which the text's baseline starts relative to the location.*/
	private var baselinePoint = Point2D.ZERO

	/** If `true`, the text is drawn in background color (only with [DrawContext.useContextColors]).*/
	var inverse: Boolean = false

	init {
		displayableText = calculateDisplayableText()
		updateGeometry()
	}

	/** ---- [Drawable] */

	override val boundingBox: Rectangle2D
		get() = rotation.rotateRectangleAround(location, bounds)

	override fun contains(x: Double, y: Double): Boolean {
		return bounds.contains(x, y)
	}

	override val canMirror: Boolean get() = true

	override fun mirrorHorizontally(x: Double) {
		location = location.mirrorHorizontally(x)
		horizontalAlignment = horizontalAlignment.opposite()
		updateGeometry()
	}

	override fun mirrorVertically(y: Double) {
		location = location.mirrorVertically(y)
		verticalAlignment = verticalAlignment.opposite()
		updateGeometry()
	}

	override fun draw(context: DrawContext) {
		drawImpl(displayableText, context)
	}

	fun draw(text: FormattedText, context: DrawContext) {
		drawImpl(text, context)
	}

	private fun drawImpl(lText: FormattedText, context: DrawContext) {
		if (StringUtils.isBlank(lText.text)) {
			return
		}

		val oldColor = context.g.color

		DrawModule.drawDebugBoundingBox(this, context.g, Color.GRAY)

		context.g.color = if (context.useContextColors) {
			if (inverse) {
				context.color!!.backgroundColor
			} else {
				context.color!!.textColor
			}
		} else {
			color ?: context.g.color
		}

		context.g.font = font
		drawTextRotated(lText, context)
		context.g.color = oldColor
	}

	private fun drawTextRotated(lText: FormattedText, context: DrawContext) {
		rotationDisplayStrategy.beforeDraw(context, this)

		context.g.translate(location.x, location.y)
		context.g.rotate(rotation.angle)
		context.g.translate(-location.x, -location.y)

		context.g.drawString(lText.text, baselinePoint.x.toInt(), baselinePoint.y.toInt())

		if (lText.allNegated) {
			context.g.stroke = OVERLINE_STROKE
			context.g.drawLine(bounds.minX + 1, bounds.minY + 1, bounds.maxX - 1, bounds.minY + 1)
		}

		context.g.translate(location.x, location.y)
		context.g.rotate(-rotation.angle)
		context.g.translate(-location.x, -location.y)

		rotationDisplayStrategy.afterDraw(context, this)
	}

	/** ---- [Label] */

	private fun updateGeometry() {
		val textRenderInfo = TextRenderInfoFactory.measureSingleLineText(displayableText.text, font)

		bounds.setFrame(
			location.x + horizontalAlignment.getX(textRenderInfo.textBounds) - BOUNDS_INSET,
			location.y - verticalAlignment.getY(textRenderInfo.textBounds) - BOUNDS_INSET,
			textRenderInfo.textBounds.width + 2 * BOUNDS_INSET,
			textRenderInfo.textBounds.height + 2 * BOUNDS_INSET
		)

		// 2 is a magic number derived from manual/visual optimization
		baselinePoint = Point2D(bounds.x + BOUNDS_INSET, bounds.y + textRenderInfo.ascent + 2)

		invalidate()
		update()
		validate()
	}

	private fun calculateDisplayableText() = FormattedText.replaceNegation(text)
}