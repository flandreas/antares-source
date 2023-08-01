package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape

/**
 * The name of the [Boolean] in [Properties] to activate tool tips with information
 * only displayed to beginner users.
 */
const val PROP_BEGINNER_HELP_TOOLTIP = "base.beginnerHelpTooltip"

/**
 * A location-sensitive, textual description of an object that can be displayed as popup in a view
 * when the user hovers over the object with the mouse.
 *
 * @param sourceRect the bounding box of the object that produced this [Tooltip]. Used to place
 * the [Tooltip] view beneath or above it. Point-like source objects can use [Rectangle2D.pointLike].
 */
data class Tooltip(val text: String, var sourceRect: RectangularShape) {
    constructor(text: String, x: Double, y: Double): this(text, Rectangle2D.pointLike(Point2D(x, y)))
}