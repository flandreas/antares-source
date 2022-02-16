package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.animation.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.View
import kotlin.math.abs

/**
 * Represents a translation of a [Point2D] in model space to a [Point2D] in view space
 * while the scene being zoomed by [zoomFactor].
 */
data class ZoomedPointTranslation(
	val modelPoint: Point2D,
	val viewPoint: Point2D,
	val zoomFactor: Double
)

/**
 * Animates a [ZoomedPointVoyage].
 */
class ZoomedPointVoyageAnimation(
	view: View<*>,
	duration: Double,
	destination: ZoomedPointTranslation
) : AbstractAnimationTask<ZoomedPointTranslation>(
	view,
	{ view.navigator.translate(it) },
	ZoomedPointVoyage(view, destination),
	duration
)

/**
 * A [Sequence] of [ZoomedPointTranslations][ZoomedPointTranslation] representing the voyage
 * of a model point towards a destination point in view space while changing the [View]'s zoom factor
 * at each step.
 */
class ZoomedPointVoyage(
	view: View<*>,
	destination: ZoomedPointTranslation
) : Sequence<ZoomedPointTranslation> {

	private val viewLocationRange = PointRange(
		begin = view.modelToView(destination.modelPoint),
		end = destination.viewPoint
	)

	private val zoomFactorRange = DoubleRange(view.zoomFactor, destination.zoomFactor)

	private val isViewLocationRangeLead = abs(viewLocationRange.size) >= abs(zoomFactorRange.size)

	private var value: ZoomedPointTranslation? = ZoomedPointTranslation(
		destination.modelPoint, view.modelToView(destination.modelPoint), view.zoomFactor)

	override val size: Double = if (isViewLocationRangeLead) viewLocationRange.size else zoomFactorRange.size

	override fun hasNext(): Boolean = size > 0 && value != null

	override fun getNext(distance: Double): ZoomedPointTranslation {
		if (value == null) {
			throw NoSuchElementException()
		}
		val next = value
		value = calculateNext(distance)
		return next!!
	}

	override fun getCurrent(): ZoomedPointTranslation = value!!

	private fun calculateNext(distance: Double): ZoomedPointTranslation? {
		if (!viewLocationRange.hasNext() && !zoomFactorRange.hasNext()) {
			return null
		}
		if (distance == 0.0) {
			return value!!
		}

		val fraction = if (isViewLocationRangeLead) {
			distance / viewLocationRange.size
		} else {
			distance / zoomFactorRange.size
		}

		val viewLocationDistance = fraction * viewLocationRange.size
		val zoomFactorDistance = fraction * zoomFactorRange.size

		val viewLocation = if (viewLocationRange.hasNext()) {
			viewLocationRange.getNext(viewLocationDistance)
		} else {
			value!!.viewPoint
		}

		val zoomFactor = if (zoomFactorRange.hasNext()) {
			zoomFactorRange.getNext(zoomFactorDistance)
		} else {
			value!!.zoomFactor
		}

		return ZoomedPointTranslation(value!!.modelPoint, viewLocation, zoomFactor)
	}
}