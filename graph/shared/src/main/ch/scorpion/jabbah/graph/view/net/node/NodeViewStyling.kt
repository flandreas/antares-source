package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Rectangle2D

/**
 * Created by andreas on 28.02.17.
 */
interface NodeViewStyling {

    val boundingBox: Rectangle2D

    fun updateBoundingBox()

    /**
     * Draws this {@link NodeViewStyling}.
     * This method expects that the caller passes the [CompositeColor] to be used for drawing in the provided
     * [DrawContext], i.e. this method uses the color in [DrawContext.color] for drawing its foreground
     * and its background.
     */
    fun draw(context: DrawContext)
}