package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.animation.DoubleRange
import ch.scorpion.jabbah.animation.Sequence
import ch.scorpion.jabbah.base.exception.NoSuchElementException
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Represents a [Sequence] of [ZoomPan] values to be used to created animations of [ZoomPan] changes.
 *
 * @property view the [View] to be zoomed and panned
 * @property endZoomFactor the zoomFactor at the end of the animation
 * @property toBeCentered the [Point2D] within [view] that will be centered at the end of the animation
 */
class ZoomPanRange(
    private val view: View<*>,
    endZoomFactor: Double,
    private val toBeCentered: Point2D
) : Sequence<ZoomPan> {

    private val zoomRange = DoubleRange(view.zoomFactor, endZoomFactor)
    private var value: ZoomPan? = view.zoomPan.copy()

    /** ---- [Sequence] interface */

    override val size: Double
        get() = zoomRange.size

    override fun hasNext(): Boolean {
        return value != null
    }

    override fun getNext(distance: Double): ZoomPan {
        if (value == null) {
            throw NoSuchElementException()
        }
        val next = value
        calculateNext(distance)
        return next!!
    }

    override fun getCurrent(): ZoomPan {
        return value!!
    }

    /** ---- [ZoomPanRange] */

    private fun calculateNext(distance: Double) {
        if (!zoomRange.hasNext()) {
            value = null
            return
        }

        val nextZoomFactor: Double = zoomRange.getNext(distance)
        val currCenterView: Point2D = view.modelToView(toBeCentered, view.zoomFactor)
        val nextCenterView: Point2D = view.modelToView(toBeCentered, nextZoomFactor)
        val deltaView = Point2D(nextCenterView.x - currCenterView.x, nextCenterView.y - currCenterView.y)

        value = ZoomPan(
            view,
            nextZoomFactor,
            Point2D(
                view.zoomPan.panOrigin.x + 2 * deltaView.x / nextZoomFactor,
                view.zoomPan.panOrigin.y + 2 * deltaView.y / nextZoomFactor)
        )
    }
}