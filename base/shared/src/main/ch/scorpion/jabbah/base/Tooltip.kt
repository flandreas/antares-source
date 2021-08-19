package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.text.StyledText

/**
 * The name of the [Boolean] in [Properties] to activate tool tips with information
 * only displayed to beginner users.
 */
const val PROP_BEGINNER_HELP_TOOLTIP = "base.beginnerHelpTooltip"

/**
 * A location-sensitive, textual description of an object that can be displayed as popup in a view
 * when the user hovers over the object with the mouse.
 *
 * @param location the location where the [Tooltip] is to be displayed, which is not necessarily
 * at the mouse location. [Tooltip] sources might wish to display the [Tooltip] at the same
 * location beneath it as long as the mouse is inside the source.
 * [location] is variable to support updates of cached [Tooltip] instances
 */
data class Tooltip(val text: StyledText, var location: Point2D) {
    constructor(text: StyledText, x: Double, y: Double): this(text, Point2D(x, y))
}