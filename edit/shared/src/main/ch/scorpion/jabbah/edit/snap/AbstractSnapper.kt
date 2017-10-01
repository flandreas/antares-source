package ch.scorpion.jabbah.edit.snap

import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.edit.*

/**
 * An abstract implementation of the [Snapper] interface that can be used as a base class for developing custom
 * [Snapper]s.
 */
abstract class AbstractSnapper(override var snapEnabled: Boolean = true) : AbstractDrawable(), Snapper {

    /** ---- [Snapper] interface */

    override fun snap(x: Double, y: Double, result: SnapResult) {
        if (snapEnabled) {
            snapX(x, result)
            snapY(y, result)
        }
    }

    override fun snapX(x: Double, result: SnapResult) {
        if (snapEnabled) {
            val newX = doSnapX(SnappableXCoordinate(x), 0.0)
            if (newX != Double.MAX_VALUE) {
                result.addDx(newX - x, newX, this)
            }
        }
    }

    override fun snapY(y: Double, result: SnapResult) {
        if (snapEnabled) {
            val newY = doSnapY(SnappableYCoordinate(y), 0.0)
            if (newY != Double.MAX_VALUE) {
                result.addDy(newY - y, newY, this)
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

        for (i in snappableX.indices) {
            val newX = doSnapX(snappableX[i], dx)
            val dX = newX - (snappableX[i].x + dx)
            if (Math.abs(dX) < Math.abs(minSnapDX)) {
                minSnapX = newX
                minSnapDX = dX
            }
        }
        if (minSnapDX != Double.MAX_VALUE) {
            result.addDx(minSnapDX, minSnapX, this)
        }
    }

    override fun snapY(snappable: Snappable, dy: Double, result: SnapResult) {
        if (!snapEnabled) {
            return
        }

        val snappableY = snappable.snappableY
        var minSnapDY = Double.MAX_VALUE
        var minSnapY = 0.0

        for (i in snappableY.indices) {
            val newY = doSnapY(snappableY[i], dy)
            val dY = newY - (snappableY[i].y + dy)
            if (Math.abs(dY) < Math.abs(minSnapDY)) {
                minSnapY = newY
                minSnapDY = dY
            }
        }
        if (minSnapDY != Double.MAX_VALUE) {
            result.addDy(minSnapDY, minSnapY, this)
        }
    }

    override fun getSnapHighlightX(x: Double, y: Double): Unzoomable? {
        return null
    }

    override fun getSnapHighlightY(x: Double, y: Double): Unzoomable? {
        return null
    }

    /** ---- [AbstractSnapper] */

    /**
     * Snaps the specified x coordinate.

     * @param x the x coordinate to be snapped by this [Snapper].
     * @return the snapped x coordinate, or [Double.MAX_VALUE] if the x coordinate
     *      could not be snapped by this [Snapper], because it is out of range.
     */
    protected abstract fun doSnapX(initSnappableX: SnappableX, initDx: Double): Double

    /**
     * Snaps the specified y coordinate.
     *
     * @param y the y coordinate to be snapped by this [Snapper].
     * @return the snapped y coordinate, or [Double.MAX_VALUE] if the y coordinate
     *      could not be snapped by this [Snapper], because it is out of range.
     */
    protected abstract fun doSnapY(initSnappableY: SnappableY, initDy: Double): Double

}