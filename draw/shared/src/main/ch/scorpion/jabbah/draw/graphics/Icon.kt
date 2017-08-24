package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext

/**
 * An [Icon] is a rectangular graphical object that used methods of [Graphics2D] for drawing its representation.
 * In contrast to [Image], which represents bitmap data that doesn't zoom nice, [Icon]s are used for
 * displaying small graphical information within a [View].
 */
interface Icon {

    val dim: Dimension2D

    /**
     * Draws this [Icon] using the specified [DrawContext].
     * @param location the location of the upper-left corner relative to the current origin of [context].
     */
    fun draw(context: DrawContext, location: Point2D)
}