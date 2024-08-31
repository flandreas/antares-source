package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.View
import kotlin.math.abs

object ArrowBubblePositioner {

	/** The vertical distance between the described object's boundary and the tip of the [ArrowBubble].*/
	const val DISTANCE = 10

	/** The minimum distance between the described object's boundary and the border of the [View].*/
	const val MIN_VIEW_DISTANCE = 5

	fun position(content: RectangularDrawable, describable: RectangularShape, view: View<*>, preferredBelow: Boolean): ArrowBubblePosition {
		val horizontal = positionHorizontally(content, describable, view)
		val positionY = positionY(content, describable, view, preferredBelow)
		val belowLocation = positionY > describable.maxY

		return ArrowBubblePosition(
			view.modelToDevice(Point2D(horizontal.coordinate, positionY)),
			belowLocation = belowLocation,
			rightOfLocation = horizontal.defaultPosition)
	}

	private fun positionHorizontally(content: RectangularDrawable, describable: RectangularShape, view: View<*>): PositionInfo {
		val width = arrowBubbleWidth(content.width)
		val centerXView = view.modelToViewX(describable.centerX)
		val overlapRight = (centerXView + width - ArrowBubble.NARROW_WIDTH - view.width).coerceAtLeast(0.0)
		val overlapLeft = abs((centerXView - width + ArrowBubble.NARROW_WIDTH).coerceAtMost(0.0))
		val rightOfLocation = overlapRight <= 0.0 || overlapRight <= overlapLeft

		var positionX = describable.centerX
		if (rightOfLocation) {
			if (overlapRight > 0) {
				positionX -= (overlapRight + MIN_VIEW_DISTANCE)
			}
		} else {
			if (overlapLeft > 0) {
				positionX += (overlapLeft + MIN_VIEW_DISTANCE)
			}
		}

		return PositionInfo(positionX, rightOfLocation)
	}

	private fun positionY(content: RectangularDrawable, describable: RectangularShape, view: View<*>, preferredBelow: Boolean): Double {
		return if (preferredBelow) {
			preferredBelowPositionY(content, describable, view)
		} else {
			preferredAbovePositionY(content, describable, view)
		}
	}

	private fun preferredBelowPositionY(content: RectangularDrawable, describable: RectangularShape, view: View<*>): Double {
		val height = arrowBubbleHeight(content.height)
		return if (view.modelToViewY(describable.maxY) + DISTANCE + height <= view.height.toDouble()) {
			describable.maxY + DISTANCE
		} else {
			describable.minY - DISTANCE
		}
	}

	private fun preferredAbovePositionY(content: RectangularDrawable, describable: RectangularShape, view: View<*>): Double {
		val height = arrowBubbleHeight(content.height)
		return if (view.modelToViewY(describable.minY) - DISTANCE - height >= 0) {
			describable.minY - DISTANCE
		} else {
			describable.maxY + DISTANCE
		}
	}

	private fun arrowBubbleWidth(contentWidth: Double): Double =
		contentWidth + 2 * ArrowBubble.INSET

	private fun arrowBubbleHeight(contentHeight: Double): Double =
		contentHeight + 2 * ArrowBubble.INSET + ArrowBubble.TIP_HEIGHT

	private data class PositionInfo(val coordinate: Double, val defaultPosition: Boolean)
}