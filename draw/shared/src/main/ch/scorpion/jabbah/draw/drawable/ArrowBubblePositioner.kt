package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.View

object ArrowBubblePositioner {

	/** The vertical distance between the described object's boundary and the tip of the [ArrowBubble].*/
	const val DISTANCE = 10

	fun position(content: RectangularDrawable, describable: RectangularShape, view: View<*>, preferredBelow: Boolean): ArrowBubblePosition {
		val rightOfLocation = rightOfLocation(content, describable, view)
		val positionX = describable.centerX
		val positionY = positionY(content, describable, view, preferredBelow)
		val belowLocation = positionY > describable.maxY

		return ArrowBubblePosition(
			view.modelToView(Point2D(positionX, positionY)),
			belowLocation = belowLocation,
			rightOfLocation = rightOfLocation)
	}

	private fun rightOfLocation(content: RectangularDrawable, describable: RectangularShape, view: View<*>): Boolean {
		val width = arrowBubbleWidth(content.width)
		return view.modelToViewX(describable.centerX) + width - ArrowBubble.NARROW_WIDTH <= view.width.toDouble()
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
}