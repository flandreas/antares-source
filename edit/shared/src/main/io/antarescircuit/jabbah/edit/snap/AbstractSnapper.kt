package io.antarescircuit.jabbah.edit.snap

import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.edit.*
import kotlin.math.abs

/**
 * An abstract implementation of the [Snapper] interface that can be used as a base class for developing custom
 * [Snapper]s.
 */
abstract class AbstractSnapper(
	override var snapEnabled: Boolean = true
) : AbstractDrawable(), Snapper {

	/** ---- [Snapper] interface */

	override fun snap(x: Double, y: Double, result: SnapResult) {
		if (snapEnabled) {
			snapX(x, result)
			snapY(y, result)
		}
	}

	override fun snapX(x: Double, result: SnapResult) {
		if (snapEnabled) {
			doSnapX(SnappableXCoordinate(x), 0.0)?.let {
				result.addDx(it.value - x, it.value, this)
				result.snappableX = it.snappable
			}
		}
	}

	override fun snapY(y: Double, result: SnapResult) {
		if (snapEnabled) {
			doSnapY(SnappableYCoordinate(y), 0.0)?.let {
				result.addDy(it.value - y, it.value, this)
				result.snappableY = it.snappable
			}
		}
	}

	override fun snap(snappable: Snappable, dx: Double, dy: Double, result: SnapResult) {
		if (snapEnabled) {
			snapX(snappable, dx, result)
			snapY(snappable, dy, result)
		}
	}

	override fun snapX(snappable: Snappable, dx: Double, result: SnapResult) {
		if (!snapEnabled) {
			return
		}

		val snappableX = snappable.snappableX
		var minSnapDX = Double.MAX_VALUE
		var minSnapX = 0.0
		var minSnappable: Snappable? = null

		for (i in snappableX.filterIndexed(snappableXFilter).indices) {
			doSnapX(snappableX[i], dx)?.let {
				val dX = it.value - (snappableX[i].x + dx)
				if (abs(dX) < abs(minSnapDX)) {
					minSnapX = it.value
					minSnapDX = dX
					minSnappable = it.snappable
				}
			}
		}
		if (minSnapDX != Double.MAX_VALUE) {
			result.addDx(minSnapDX, minSnapX, this)
			result.snappableX = minSnappable
		}
	}

	override fun snapY(snappable: Snappable, dy: Double, result: SnapResult) {
		if (!snapEnabled) {
			return
		}

		val snappableY = snappable.snappableY
		var minSnapDY = Double.MAX_VALUE
		var minSnapY = 0.0
		var minSnappable: Snappable? = null

		for (i in snappableY.filterIndexed(snappableYFilter).indices) {
			doSnapY(snappableY[i], dy)?.let {
				val dY = it.value - (snappableY[i].y + dy)
				if (abs(dY) < abs(minSnapDY)) {
					minSnapY = it.value
					minSnapDY = dY
					minSnappable = it.snappable
				}
			}
		}
		if (minSnapDY != Double.MAX_VALUE) {
			result.addDy(minSnapDY, minSnapY, this)
			result.snappableY = minSnappable
		}
	}

	/** ---- [AbstractSnapper] */

	protected open val snappableXFilter: (Int, SnappableX) -> Boolean = { _,_ -> true }
	protected open val snappableYFilter: (Int, SnappableY) -> Boolean = { _,_ -> true }

	protected data class DoSnapResult(val value: Double, val snappable: Snappable?)

	/**
	 * Snaps the specified x coordinate.
	 * @return the snapped x coordinate along with the [Snapper] that snapped it, or `null`
	 * if no snapping occurred
	 */
	protected abstract fun doSnapX(initSnappableX: SnappableX, initDx: Double): DoSnapResult?

	/**
	 * Snaps the specified y coordinate.
	 * @return the snapped y coordinate along with the [Snapper] that snapped it, or `null`
	 * if no snapping occurred
	 */
	protected abstract fun doSnapY(initSnappableY: SnappableY, initDy: Double): DoSnapResult?
}