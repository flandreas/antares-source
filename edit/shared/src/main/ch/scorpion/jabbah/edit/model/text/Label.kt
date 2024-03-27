package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.Mirrorable
import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
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
	ownerRotation: Rotation = Rotation.R0,
	displayableText: RichTextDrawable = RichTextDrawable.of(text ?: "", font)
) : AbstractDrawable(), Mirrorable, Locatable {

	companion object {
		private val DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.CENTER
		private val DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.CENTER
		private const val BOUNDS_INSET = 1
	}

	var text: String = text ?: ""
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				displayableText = RichTextDrawable.of(value, font)
				updateGeometry()
			}
		}

	var font: Font = font
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				displayableText = RichTextDrawable.of(text, field)
				updateGeometry()
			}
		}

	override var location: Point2D = location
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
	private var displayableText: RichTextDrawable = displayableText

	/** The [Rectangle2D] that contains the text entirely.*/
	val bounds = Rectangle2D()

	/** If `true`, the text is drawn in background color (only with [DrawContext.useContextColors]).*/
	var inverse: Boolean = false

	init {
		updateGeometry()
	}

	/** ---- [Drawable] */

	override val boundingBox: Rectangle2D
		get() = rotation.rotateRectangleAround(location, bounds)

	override fun contains(x: Double, y: Double): Boolean {
		return bounds.contains(x, y)
	}

	override fun draw(context: DrawContext) {
		drawImpl(displayableText, context)
	}

	fun draw(text: RichTextDrawable, context: DrawContext) {
		positionDisplayableText(text)
		drawImpl(text, context)
	}

	private fun drawImpl(richText: RichTextDrawable, context: DrawContext) {
		val oldColor = context.g.color

		DrawModule.drawDebugBoundingBox(this, context.g, Color.GRAY)
		DrawModule.drawDebugBoundingBoxLocation(location, context, Color.GREEN)

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
		drawTextRotated(richText, context)

		context.g.color = oldColor
	}

	private fun drawTextRotated(richText: RichTextDrawable, context: DrawContext) {
		rotationDisplayStrategy.beforeDraw(context, this)

		context.g.translate(location.x, location.y)
		context.g.rotate(rotation.angle)
		context.g.translate(-location.x, -location.y)

		richText.draw(context)

		context.g.translate(location.x, location.y)
		context.g.rotate(-rotation.angle)
		context.g.translate(-location.x, -location.y)

		rotationDisplayStrategy.afterDraw(context, this)
	}

	/** ---- [Mirrorable] */

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

	/** ---- [Label] */

	private fun updateGeometry() {
		invalidate()

		positionDisplayableText(displayableText)
		bounds.setFrame(displayableText.bounds)

		update()
		validate()
	}

	private fun positionDisplayableText(text: RichTextDrawable) {
		// 1 is a magic number derived from manual/visual optimization
		text.location = Point2D(
			location.x + horizontalAlignment.getX(text.baselineRect) - BOUNDS_INSET,
			location.y - verticalAlignment.getY(text.baselineRect) + 1 - BOUNDS_INSET
		)
	}
}