package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.geom.Point2D

/**
 * A location-sensitive, textual description of an object that can be displayed as popup in a view
 * when the user hovers over the object with the mouse.
 */
data class Tooltip(val text: String, val location: Point2D) {
    constructor(text: String, x: Double, y: Double): this(text, Point2D(x, y))
}