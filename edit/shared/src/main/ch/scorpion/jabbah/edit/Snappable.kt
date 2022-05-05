package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.drawable.Unzoomable

/**
 * [Snappable] is implemented by [Component]s that want to be snapped by [Snapper]s.
 *
 * The two methods return the x and y coordinates at which this [Snappable] wants the snapping to occur. For
 * example, a rectangular [Component] would return the x coordinate of its left and right edges, and the y
 * coordinate of its top and bottom edges. Additionally, a rectangle could also return the x and y coordinates of its
 * center, which would direct the [Snapper] to snap the rectangle at the middle of its edges as well.
 */
interface Snappable {

	companion object {
		val EMPTY_X = arrayOf<SnappableX>()
		val EMPTY_Y = arrayOf<SnappableY>()
	}

    /**
     * Holds the x-coordinates at which this [Snappable] wants to be snapped, or an empty array if this [Snappable]
     * doesn't want to be snapped at x-coordinates.
     */
    val snappableX: Array<SnappableX>

    /**
     * Holds the y-coordinates at which this [Snappable] wants to be snapped, or an empty array if this [Snappable]
     * doesn't want to be snapped at y-coordinates.
     */
    val snappableY: Array<SnappableY>

    /** Returns a visual indication of the x snapping, or `null` if default visual indication should be used.*/
    fun getSnapHighlightX(x: Double, y: Double): Unzoomable? = null

	/** Returns a visual indication of the y snapping, or `null` if default visual indication should be used.*/
	fun getSnapHighlightY(x: Double, y: Double): Unzoomable? = null
}

interface SnappableX {

    val x: Double

    fun accept(other: SnappableX): Boolean
}

interface SnappableY {

    val y: Double

    fun accept(other: SnappableY): Boolean
}

data class SnappableXCoordinate(override var x: Double) : SnappableX {
    override fun accept(other: SnappableX): Boolean = true
}

data class SnappableYCoordinate(override var y: Double) : SnappableY {
    override fun accept(other: SnappableY): Boolean = true
}