package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ViewNavigator
import ch.scorpion.jabbah.draw.ZoomPan
import kotlin.math.max
import kotlin.math.min

/**
 * A default implementation of the [ViewNavigator] interface.
 */
class ViewNavigatorImpl(
	private val view: View<*>,
	private val properties: Properties = BaseModule.properties
) : ViewNavigator {

	private companion object {

		val LOG by logger(ViewNavigatorImpl::class)

		/**
		 * The number of pixels (in view space) to be left free on each side of the View when calculating
		 * the fitting zoom factor.
		 */
		const val FIT_ZOOM_INSET: Int = 20
	}

	private val defaultZoomFactor: Double get() =
		(properties.getFloat(View.PROP_DEFAULT_ZOOM_FACTOR) * max(1, view.canvas.devicePixelRatio)).toDouble()

	/** ---- [ViewNavigator] interface */

	override fun setZoomFactor(zoomFactor: Double) {
		setZoomPan(ZoomPan(view, zoomFactor, view.zoomPan.panOrigin))
	}

	override fun addZoomFactor(delta: Double) {
		val newZoomFactor = view.zoomFactor + delta
		if (isZoomFactorInValidRange(newZoomFactor)) {
			setZoomFactor(newZoomFactor)
		}
	}

	override fun multiplyZoomFactor(factor: Double) {
		val newZoomFactor = view.zoomFactor * factor
		if (isZoomFactorInValidRange(newZoomFactor)) {
			setZoomFactor(newZoomFactor)
		}
	}

	override fun panBy(dx: Int, dy: Int) {
		setZoomPan(ZoomPan(view, view.zoomFactor, Point2D(
			view.zoomPan.panOrigin.x - dx / view.zoomFactor,
			view.zoomPan.panOrigin.y - dy / view.zoomFactor)))
	}

	override fun setPanOrigin(p: Point2D) {
		setZoomPan(ZoomPan(view, view.zoomPan.zoomFactor, p))
	}

	override fun setZoomPan(zoomPan: ZoomPan) {
		view.zoomPan = zoomPan
	}

	override fun panCenter() {
		panCenter(view.zoomFactor)
	}

	override fun panCenterDefault() {
		panCenter(defaultZoomFactor)
	}

	override fun panCenter(zoomFactor: Double) {
		setZoomPan(ZoomPan(view, zoomFactor, calculateContentCenterPan(zoomFactor)))
	}

	override fun fit() {
		val zoomFactor = calculateFitZoomFactor()
		setZoomPan(ZoomPan(view, zoomFactor, calculateContentCenterPan(zoomFactor)))
	}

	override fun fitMaxNormal() {
		val zoomFactor = min(defaultZoomFactor, calculateFitZoomFactor())
		setZoomPan(ZoomPan(view, zoomFactor, calculateContentCenterPan(zoomFactor)))
	}

	/** ---- [ViewNavigatorImpl] */

	private fun calculateContentCenterPan(zoomFactor: Double): Point2D {
		val bounds = view.contentBounds
		val newCenter = view.modelToView(Point2D(bounds.centerX, bounds.centerY), zoomFactor)
		val result = Point2D(
			view.zoomPan.panOrigin.x + (newCenter.x - view.width / 2.0) / zoomFactor,
			view.zoomPan.panOrigin.y + (newCenter.y - view.height / 2.0) / zoomFactor)

		LOG.trace("center Pan for content $bounds in view ${view.canvas.dimension} is $result")

		return result
	}

	private fun calculateFitZoomFactor(): Double {
		val bounds = view.contentBounds
		if (bounds.width == 0.0 || bounds.height == 0.0) {
			return defaultZoomFactor
		}
		if (view.width == 0 || view.height == 0) {
			return defaultZoomFactor
		}

		return min(
			(view.width - 2 * FIT_ZOOM_INSET) / bounds.width,
			(view.height - 2 * FIT_ZOOM_INSET) / bounds.height)
	}

	private fun isZoomFactorInValidRange(zoomFactor: Double): Boolean {
		return zoomFactor >= BaseModule.properties.getFloat(View.PROP_MIN_ZOOM_FACTOR)
			&& zoomFactor <= BaseModule.properties.getFloat(View.PROP_MAX_ZOOM_FACTOR)
	}
}