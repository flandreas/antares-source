package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * A [Drawable] used to highlight the point where a new connecting [EdgeView] starts or ends when the mouse
 * is being moved or dragged.
 */
interface ConnectionPointHighlight : Unzoomable {
    var location: Point2D
}