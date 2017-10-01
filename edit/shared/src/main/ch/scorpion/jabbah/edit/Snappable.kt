package ch.scorpion.jabbah.edit

/**
 * [Snappable] is implemented by [Component]s that want to be snapped by [Snapper]s.
 *
 * The two methods return the x and y coordinates at which this [Snappable] wants the snapping to occur. For
 * example, a rectangular [Component] would return the x coordinate of its left and right edges, and the y
 * coordinate of its top and bottom edges. Additionally, a rectangle could also return the x and y coordinates of its
 * center, which would direct the [Snapper] to snap the rectangle at the middle of its edges as well.
 */
interface Snappable {

    /**
     * Holds the x-coordinates at which this [Snappable] wants to be snapped, or an empty array if this [Snappable]
     * doesn't want to be snapped at x-coordinates.
     */
    val snappableX: DoubleArray

    /**
     * Holds the y-coordinates at which this [Snappable] wants to be snapped, or an empty array if this [Snappable]
     * doesn't want to be snapped at y-coordinates.
     */
    val snappableY: DoubleArray
}