package ch.scorpion.jabbah.edit.snap

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.base.geom.Rectangle2D

/**
 * An interface for plugable classes that actually paint a [GridImpl] within the shape of a rectangle.
 */
interface GridPainter {

    /** The distance of the painted grid dots in x direction. */
    var distanceX: Double

    /** The distance of the painted grid dots in y direction. */
    var distanceY: Double

    /** The zoom properties to be used for painting.*/
    var zoomPan: ZoomPan?

    /** Paints the [Grid] within the specified rectangular bounds.*/
    fun paint(context: DrawContext, rect: Rectangle2D)

}