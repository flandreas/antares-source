package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.View
import kotlin.math.abs

/**
 * Calculates the position of a content [Dimension2D] to be used as attendant of a [RectangularShape],
 * typically to display some additional description for the attendee (tooltips, popup keyboards etc.).
 *
 * Tries to align the horizontal center of the content with the horizontal center of the attendee,
 * but applies a horizontal adjustment if part of the attendant is clipped by the [View] boundary.
 */
object DrawableAttendantPositioner {

	/** In view space */
	private const val MIN_VIEW_DISTANCE = 5

	/**
	 * @property y the top Y if [below] is `true`, or the bottom Y if [below] is `false`
	 */
	private data class VerticalPosition(val y: Double, val below: Boolean)

	/**
	 * Calculates the position for [attendant].
	 *
	 * @param attendant the [Dimension2D] whose position is to be calculated
	 * @param attendee the content to be attended by [attendant]
	 * @param view restricts positioning by its borders
	 * @param preferredBelow `true` if [attendant] should be placed below [attendee], if possible
	 * @param distance the vertical distance between [attendant] and [attendee]
	 */
	fun position(
		attendant: Dimension2D,
		attendee: RectangularShape,
		view: View<*>,
		preferredBelow: Boolean,
		distance: Int
	): Point2D {
		val verticalPosition = calculateVerticalPosition(attendant, attendee, view, preferredBelow, distance)
		val contentCenterX = calculateContentCenterX(attendant, attendee, view)

		// Calculate the top left location
		return if (verticalPosition.below) {
			Point2D(contentCenterX - attendant.width / 2, verticalPosition.y)
		} else {
			Point2D(contentCenterX - attendant.width / 2, verticalPosition.y - attendant.height)
		}
	}

	private fun calculateContentCenterX(
		attendant: Dimension2D,
		attendee: RectangularShape,
		view: View<*>,
	): Double {
		val centerXView = view.modelToView(attendee.center)
		val widthHalfView = view.modelToViewLength(attendant.width / 2)
		val rightView = centerXView.x + widthHalfView
		val leftView = centerXView.x - widthHalfView

		val overlapRightView = (rightView - (view.width - MIN_VIEW_DISTANCE)).coerceAtLeast(0.0)
		val overlapLeftView = abs((leftView - MIN_VIEW_DISTANCE).coerceAtMost(0.0))

		var positionX = attendee.centerX
		if (overlapRightView > 0 && overlapRightView > overlapLeftView) {
			// Correct to the left
			positionX -= view.viewToModelLength(overlapRightView)
		} else if (overlapLeftView > 0) {
			// Correct to the right
			positionX += view.viewToModelLength(overlapLeftView)
		}

		return positionX
	}

	private fun calculateVerticalPosition(
		attendant: Dimension2D,
		attendee: RectangularShape,
		view: View<*>,
		preferredBelow: Boolean,
		distance: Int
	): VerticalPosition {
		return if (preferredBelow) {
			preferredBelowPositionY(attendant, attendee, view, distance)
		} else {
			preferredAbovePositionY(attendant, attendee, view, distance)
		}
	}

	private fun preferredBelowPositionY(attendant: Dimension2D, attendee: RectangularShape, view: View<*>, distance: Int): VerticalPosition {
		return if (view.modelToViewY(attendee.maxY + distance) + view.modelToViewLength(attendant.height) <= view.height - MIN_VIEW_DISTANCE) {
			VerticalPosition(attendee.maxY + distance, below = true)
		} else {
			VerticalPosition(attendee.minY - distance, below = false)
		}
	}

	private fun preferredAbovePositionY(attendant: Dimension2D, attendee: RectangularShape, view: View<*>, distance: Int): VerticalPosition {
		return if (view.modelToViewY(attendee.minY - distance) - view.modelToViewLength(attendant.height) >= MIN_VIEW_DISTANCE) {
			VerticalPosition(attendee.minY - distance, below = false)
		} else {
			VerticalPosition(attendee.maxY + distance, below = true)
		}
	}
}