package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ViewNavigator
import ch.scorpion.jabbah.draw.ViewTransformation
import ch.scorpion.jabbah.draw.ZoomPan
import kotlin.math.min

/**
 * A default implementation of the [ViewNavigator] interface.
 */
class ViewNavigatorImpl(
	private val view: View<*>,
	private val affineTransformFactory: () -> AffineTransform,
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

	private val defaultZoomFactor: Double get() = properties.getFloat(View.PROP_DEFAULT_ZOOM_FACTOR).toDouble()

	/** ---- [ViewNavigator] interface */

	override fun createTransformation(zoomFactor: Double): ViewTransformation =
		createTransformation(ZoomPan(view, zoomFactor, view.zoomPan.panOrigin))

	private fun createTransformation(zoomPan: ZoomPan): ViewTransformation =
		ViewTransformation(
			zoomPan,
			affineTransformFactory.invoke().apply {
				scale(zoomPan.zoomFactor, zoomPan.zoomFactor)
				translate(zoomPan.panOrigin.negate)
			}
	)

	override fun setZoomFactor(zoomFactor: Double, zoomLocation: Point2D?) {
		val effZoomLocation = zoomLocation ?: view.center

		if (!isZoomFactorInValidRange(zoomFactor)) {
			return
		}

		val zoomLocationBeforeM = view.viewToModel(effZoomLocation)
		val zoomLocationAfterM = view.viewToModel(effZoomLocation, zoomFactor)
		val offset = zoomLocationBeforeM.subtract(zoomLocationAfterM)

		val zoomPan = ZoomPan(view, zoomFactor, view.zoomPan.panOrigin.add(offset))
		view.transformation = createTransformation(zoomPan)
	}

	override fun translate(translation: ZoomedPointTranslation) {
		val locationAfterZoomV = translation.modelPoint.multiply(translation.zoomFactor)
		val offsetV = translation.viewPoint.subtract(locationAfterZoomV)
		val offsetM = offsetV.multiply(1 / translation.zoomFactor).negate

		val zoomPan = ZoomPan(view, translation.zoomFactor, offsetM)

		view.transformation = createTransformation(zoomPan)
	}

	override fun multiplyZoomFactor(factor: Double, zoomLocation: Point2D?) {
		setZoomFactor(view.zoomFactor * factor, zoomLocation)
	}

	override fun panBy(dx: Int, dy: Int) {
		setZoomPan(ZoomPan(view, view.zoomFactor, Point2D(
			view.zoomPan.panOrigin.x - dx / view.zoomFactor,
			view.zoomPan.panOrigin.y - dy / view.zoomFactor)))
	}

	override fun setPanOrigin(p: Point2D) {
		setZoomPan(ZoomPan(view, view.zoomPan.zoomFactor, p))
	}

	private fun setZoomPan(zoomPan: ZoomPan) {
		view.transformation = createTransformation(zoomPan)
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
		val zoomFactor = calculateFixMaxNormalZoomFactor()
		setZoomPan(ZoomPan(view, zoomFactor, calculateContentCenterPan(zoomFactor, fixMaxNormal = true)))
	}

	override fun calculateFixMaxNormalZoomFactor(): Double =
		min(defaultZoomFactor, calculateFitZoomFactor())

	/** ---- [ViewNavigatorImpl] */

	private fun calculateContentCenterPan(zoomFactor: Double, fixMaxNormal: Boolean = false): Point2D {
		val bounds = view.contentBounds
		var newCenter = view
			.modelToView(Point2D(bounds.main.centerX, bounds.main.centerY), zoomFactor)

		if (fixMaxNormal) {
			val overlapY = view.space.area.minY + FIT_ZOOM_INSET - view.modelToView(Point2D(0.0, bounds.main.minY), zoomFactor).y
			if (overlapY > 0) {
				newCenter = newCenter.subtract(Point2D(0.0, overlapY))
			}
		}

		val result = Point2D(
			view.zoomPan.panOrigin.x + (newCenter.x - view.space.viewDimension.width / 2.0) / zoomFactor,
			view.zoomPan.panOrigin.y + (newCenter.y - view.space.viewDimension.height / 2.0) / zoomFactor)

		LOG.trace("center Pan for content $bounds in view ${view.canvas.dimension} is $result")

		return result
	}

	private fun calculateFitZoomFactor(): Double {
		val bounds = view.contentBounds.total
		if (bounds.width == 0.0 || bounds.height == 0.0) {
			return defaultZoomFactor
		}
		if (view.space.area.widthInt == 0 || view.space.area.heightInt == 0) {
			return defaultZoomFactor
		}

		return min(
			(view.space.area.widthInt - 2 * FIT_ZOOM_INSET) / bounds.width,
			(view.space.area.heightInt - 2 * FIT_ZOOM_INSET) / bounds.height)
	}

	private fun isZoomFactorInValidRange(zoomFactor: Double): Boolean =
		zoomFactor >= BaseModule.properties.getFloat(View.PROP_MIN_ZOOM_FACTOR)
			&& zoomFactor <= BaseModule.properties.getFloat(View.PROP_MAX_ZOOM_FACTOR)
}