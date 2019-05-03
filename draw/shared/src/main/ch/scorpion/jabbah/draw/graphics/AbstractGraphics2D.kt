package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.polyline.PolylineShape
import kotlin.math.PI
import kotlin.math.min

/**
 * Implements those parts of the [Graphics2D] that are platform-independent.
 */
abstract class AbstractGraphics2D : Graphics2D {

	companion object {
		private val LOG by logger(AbstractGraphics2D::class)
	}

	/** ---- [Graphics2D] */

	override fun draw(shape: Shape) {
		when (shape) {
			is Rectangle2D -> drawRect(shape)
			is RoundRectangle2D -> drawRoundRect(shape)
			is Ellipse2D -> drawEllipse(shape)
			is PolylineShape -> drawPolyline(shape)
			is Ring2D -> drawRing(shape)
			else -> {
				LOG.error("Unsupported shape $shape")
				throw IllegalArgumentException("Unsupported shape $shape")
			}
		}
	}

	override fun fill(shape: Shape) {
		when (shape) {
			is Rectangle2D -> fillRect(shape)
			is RoundRectangle2D -> fillRoundRect(shape)
			is Ellipse2D -> fillEllipse(shape)
			is PolylineShape -> fillPoyline(shape)
			is Ring2D -> drawRing(shape)
			else -> {
				LOG.error("Unsupported shape $shape")
				throw IllegalArgumentException("Unsupported shape $shape")
			}
		}
	}

	/** ---- Path rendering methods */

	abstract fun beginPath()

	abstract fun moveTo(x: Double, y: Double)

	abstract fun lineTo(x: Double, y: Double)

	abstract fun quadraticCurveTo(xc: Double, yc: Double, x1: Double, y1: Double)

	abstract fun bezierCurveTo(xc1: Double, yc1: Double, xc2: Double, yc2: Double, x1: Double, y1: Double)

	abstract fun arc(x: Double, y: Double, radius: Double, startAngle: Double, endAngle: Double, anticlockwise: Boolean = false)

	abstract fun strokePath()

	abstract fun fillPath()

	abstract fun closePath()

	/** ---- [AbstractGraphics2D] */

	protected fun drawRect(rect: Rectangle2D) {
		drawRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
	}

	protected fun fillRect(rect: Rectangle2D) {
		fillRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
	}

	protected fun drawRoundRect(rect: RoundRectangle2D) {
		drawRoundRect(rect.x, rect.y, rect.width, rect.height, rect.arcW, rect.arcH)
	}

	protected fun drawRoundRect(x: Double, y: Double, w: Double, h: Double, arcW: Double, arcH: Double) {
		beginPath()
		playRoundRect(x, y, w, h, arcW, arcH)
		strokePath()
	}

	protected fun fillRoundRect(rect: RoundRectangle2D) {
		fillRoundRect(rect.x, rect.y, rect.width, rect.height, rect.arcW, rect.arcH)
	}

	protected fun fillRoundRect(x: Double, y: Double, w: Double, h: Double, arcW: Double, arcH: Double) {
		beginPath()
		playRoundRect(x, y, w, h, arcW, arcH)
		fillPath()
	}

	protected fun drawRing(ring: Ring2D) {
		beginPath()
		playRing(ring.x, ring.y, ring.width, ring.height, ring.thickness)
	}

	protected fun drawEllipse(ellipse: Ellipse2D) {
		beginPath()
		playEllipse(ellipse.x, ellipse.y, ellipse.width, ellipse.height)
		strokePath()
	}

	protected fun fillEllipse(ellipse: Ellipse2D) {
		beginPath()
		playEllipse(ellipse.x, ellipse.y, ellipse.width, ellipse.height)
		fillPath()
	}

	protected fun drawPolyline(polyline: PolylineShape) {
		beginPath()
		playPolyline(polyline)
		strokePath()
		closePath()
	}

	protected fun fillPoyline(polyline: PolylineShape) {
		beginPath()
		playPolyline(polyline)
		fillPath()
		closePath()
	}

	protected fun playPolyline(polyline: PolylineShape) {
		if (polyline.pointsCount < 2) {
			return
		}
		moveTo(polyline.getPointAt(0).x, polyline.getPointAt(0).y)
		for (i in 1 until polyline.pointsCount) {
			lineTo(polyline.getPointAt(i).x, polyline.getPointAt(i).y)
		}
	}

	protected fun playRoundRect(x: Double, y: Double, w: Double, h: Double, arcW: Double, arcH: Double) {
		val arcWW = min(arcW, w / 2)
		val arcHH = min(arcH, h / 2)
		moveTo(x, y + arcHH)
		lineTo(x, y + h - arcHH)
		quadraticCurveTo(x, y + h, x + arcWW, y + h)
		lineTo(x + w - arcWW, y + h)
		quadraticCurveTo(x + w, y + h, x + w, y + h - arcHH)
		lineTo(x + w, y + arcHH)
		quadraticCurveTo(x + w, y, x + w - arcWW, y)
		lineTo(x + arcWW, y)
		quadraticCurveTo(x, y, x, y + arcHH)
	}

	protected fun playEllipse(x: Double, y: Double, w: Double, h: Double) {
		val kappa = 0.5522848
		val ox = (w / 2) * kappa
		val oy = (h / 2) * kappa
		val xe = x + w
		val ye = y + h
		val xm = x + w / 2
		val ym = y + h / 2

		moveTo(x, ym)
		bezierCurveTo(x, ym - oy, xm - ox, y, xm, y)
		bezierCurveTo(xm + ox, y, xe, ym - oy, xe, ym)
		bezierCurveTo(xe, ym + oy, xm + ox, ye, xm, ye)
		bezierCurveTo(xm - ox, ye, x, ym + oy, x, ym)
	}

	protected fun playRing(x: Double, y: Double, w: Double, h: Double, thickness: Double) {
		// NOTE: This implementation support only circular rings. Uses width as radius
		if (w != h) {
			LOG.warn("Graphics2DJs: requested ellipsoid ring, but only circular ring supported.")
		}
		arc(x + w / 2, y + w / 2, w / 2, 0.0, PI * 2, false)
		arc(x + w / 2, y + w / 2, w / 2 - thickness, 0.0, PI * 2, true)
	}
}