package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Path
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType

/**
 * Defines the position of an [ArrowBubble] relative to its location.
 *
 * @property location the [Point2D] at the tip of the arrow path in view coordinates
 * @property belowLocation `true` if the [ArrowBubble] is to be positioned below [location]
 * @property rightOfLocation `true` if the major part of the [ArrowBubble] is to be positioned to the right side of [location],
 * which also means that the [ArrowBubble]'s arrow tip is located at the left side of the [ArrowBubble]
 */
data class ArrowBubblePosition(
	val location: Point2D,
	val belowLocation: Boolean,
	val rightOfLocation: Boolean
)

/**
 * A [ArrowBubble] is a [Drawable] that draws a [RectangularDrawable] inside a [Path] consisting of
 * a rounded rectangle and an arrow tip pointing upwards.
 *
 * @property content the content to be drawn inside the [Path]
 * @param position defined the position of this [ArrowBubble]
 */
class ArrowBubble(
	private val content: RectangularDrawable,
	val position: ArrowBubblePosition,
	styleType: StyleType = StyleType.TOOLTIP,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractStyledDrawable(styleType, styleProvider) {

	companion object {

		/** The inset between the content and the border path.*/
		const val INSET = 8.0

		/** The height of the arrow tip.*/
		const val TIP_HEIGHT = 15.0

		/** The size of the rounded corner arcs.*/
		private const val ARC_SIZE = 5.0

		/** The base width of the arrow tip.*/
		private const val TIP_WIDTH = 15.0

		/** The width of the area from the path's narrower edge (left edge in case of [ArrowBubblePosition.rightOfLocation]) to the arrow's tip.*/
		const val NARROW_WIDTH = TIP_WIDTH / 2 + 20
	}

	/** The border path with the arrow tip pointing upwards. Expressed in relative coordinates with origin (0,0) at the arrow tip. */
	private val path: Path = createPath(position)

	/** The overall width of the path.*/
	private val width: Double get() = content.width + 2 * INSET

	private val height: Double get() = content.height + 2 * INSET + TIP_HEIGHT

	/** The width of the area from the path's wider edge (right edge in case of [ArrowBubblePosition.rightOfLocation]) to the arrow's tip.*/
	private val wideWidth: Double get() = width - NARROW_WIDTH

	init {
		content.location = calculateContentLocation(position)
		DrawableOwner(this, content)
	}

	/** ----  [AbstractDrawable] */

	override val boundingBox: RectangularShape get() = Rectangle2D(path.boundingBox)
		.expandBy(style.stroke.width.toDouble())
		.moveBy(position.location)

	override fun draw(context: DrawContext) {
		context.translated(position.location) {
			it.g.color = style.color.backgroundColor
			it.g.fill(path)
			it.g.color = style.color.foregroundColor
			it.g.stroke = style.stroke
			it.g.draw(path)

			it.g.color = style.color.textColor
			it.g.font = style.font
			content.draw(context)
		}
	}

	override fun contains(x: Double, y: Double): Boolean = path.contains(x - position.location.x, y - position.location.y)

	/** ---- [ArrowBubble] */

	/** The upper-left corner of [content] in the local, relative coordinate system. */
	private fun calculateContentLocation(position: ArrowBubblePosition): Point2D =
		if (position.belowLocation) {
			if (position.rightOfLocation) {
				Point2D(-NARROW_WIDTH + INSET, TIP_HEIGHT + INSET)
			} else {
				Point2D(-wideWidth + INSET, TIP_HEIGHT + INSET)
			}
		} else {
			if (position.rightOfLocation) {
				Point2D(-NARROW_WIDTH + INSET, -height + INSET)
			} else {
				Point2D(-wideWidth + INSET, -height + INSET)
			}
		}

	private fun createPath(position: ArrowBubblePosition): Path = if (position.belowLocation) {
		if (position.rightOfLocation) {
			createBelowRightPath()
		} else {
			createBelowLeftPath()
		}
	} else {
		if (position.rightOfLocation) {
			createAboveRightPath()
		} else {
			createAboveLeftPath()
		}
	}

	private fun createBelowRightPath(): Path =
		System.createPath()
			.moveTo(0, 0)
			.lineTo(TIP_WIDTH / 2, TIP_HEIGHT)
			.lineTo(wideWidth - ARC_SIZE, TIP_HEIGHT)
			.quadTo(wideWidth, TIP_HEIGHT, wideWidth, TIP_HEIGHT + ARC_SIZE)
			.lineTo(wideWidth, height - ARC_SIZE)
			.quadTo(wideWidth, height, wideWidth - ARC_SIZE, height)
			.lineTo(-NARROW_WIDTH + ARC_SIZE, height)
			.quadTo(-NARROW_WIDTH, height, -NARROW_WIDTH, height - ARC_SIZE)
			.lineTo(-NARROW_WIDTH, TIP_HEIGHT + ARC_SIZE)
			.quadTo(-NARROW_WIDTH, TIP_HEIGHT, -NARROW_WIDTH + ARC_SIZE, TIP_HEIGHT)
			.lineTo(-TIP_WIDTH / 2, TIP_HEIGHT)
			.close()

	private fun createBelowLeftPath(): Path =
		System.createPath()
			.moveTo(0, 0)
			.lineTo(TIP_WIDTH / 2, TIP_HEIGHT)
			.lineTo(NARROW_WIDTH - ARC_SIZE, TIP_HEIGHT)
			.quadTo(NARROW_WIDTH, TIP_HEIGHT, NARROW_WIDTH, TIP_HEIGHT + ARC_SIZE)
			.lineTo(NARROW_WIDTH, height - ARC_SIZE)
			.quadTo(NARROW_WIDTH, height, NARROW_WIDTH - ARC_SIZE, height)
			.lineTo(-wideWidth + ARC_SIZE, height)
			.quadTo(-wideWidth, height, -wideWidth, height - ARC_SIZE)
			.lineTo(-wideWidth, TIP_HEIGHT + ARC_SIZE)
			.quadTo(-wideWidth, TIP_HEIGHT, -wideWidth + ARC_SIZE, TIP_HEIGHT)
			.lineTo(-TIP_WIDTH / 2, TIP_HEIGHT)
			.close()

	private fun createAboveRightPath(): Path =
		System.createPath()
			.moveTo(0, 0)
			.lineTo(-TIP_WIDTH / 2, -TIP_HEIGHT)
			.lineTo(-NARROW_WIDTH + ARC_SIZE, -TIP_HEIGHT)
			.quadTo(-NARROW_WIDTH, -TIP_HEIGHT, -NARROW_WIDTH, -TIP_HEIGHT - ARC_SIZE)
			.lineTo(-NARROW_WIDTH, -height + ARC_SIZE)
			.quadTo(-NARROW_WIDTH, -height, -NARROW_WIDTH + ARC_SIZE, -height)
			.lineTo(wideWidth - ARC_SIZE, -height)
			.quadTo(wideWidth, -height, wideWidth, -height + ARC_SIZE)
			.lineTo(wideWidth, -TIP_HEIGHT - ARC_SIZE)
			.quadTo(wideWidth, -TIP_HEIGHT, wideWidth - ARC_SIZE, -TIP_HEIGHT)
			.lineTo(TIP_WIDTH / 2, -TIP_HEIGHT)
			.close()

	private fun createAboveLeftPath(): Path =
		System.createPath()
			.moveTo(0, 0)
			.lineTo(-TIP_WIDTH / 2, -TIP_HEIGHT)
			.lineTo(-wideWidth + ARC_SIZE, -TIP_HEIGHT)
			.quadTo(-wideWidth, -TIP_HEIGHT, -wideWidth, -TIP_HEIGHT - ARC_SIZE)
			.lineTo(-wideWidth, -height + ARC_SIZE)
			.quadTo(-wideWidth, -height, -wideWidth + ARC_SIZE, -height)
			.lineTo(NARROW_WIDTH - ARC_SIZE, -height)
			.quadTo(NARROW_WIDTH, -height, NARROW_WIDTH, -height + ARC_SIZE)
			.lineTo(NARROW_WIDTH, -TIP_HEIGHT - ARC_SIZE)
			.quadTo(NARROW_WIDTH, -TIP_HEIGHT, NARROW_WIDTH - ARC_SIZE, -TIP_HEIGHT)
			.lineTo(TIP_WIDTH / 2, -TIP_HEIGHT)
			.close()
}