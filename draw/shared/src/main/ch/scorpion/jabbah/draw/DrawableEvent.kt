package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape

/**
 * Used to notify [DrawableListener]s upon changes of a [Drawable].
 */
data class DrawableEvent(val source: Drawable, val area: RectangularShape) {
    constructor(source: Drawable) : this(source, Rectangle2D(source.boundingBox))
}