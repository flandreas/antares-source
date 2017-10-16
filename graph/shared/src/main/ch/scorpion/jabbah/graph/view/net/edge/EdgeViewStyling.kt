package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle

/** An abstraction being able to draw an [EdgeView] with a particular [NetViewStyle].*/
interface EdgeViewStyling {

    /**
     * The width of segments drawn by this [EdgeViewStyling]. Uses e.g. by [PortView]s to adjust
     * the position of external labels.
     */
    val width: Int

    val boundingBox: Rectangle2D

    /**
     * Draws this [EdgeViewStyling].
     * This method expects that the caller passes the [CompositeColor] to be used for drawing in the provided
     * [DrawContext], i.e. this method uses the color in [DrawContext.color] for drawing its foreground
     * and its background.
     */
    fun draw(context: DrawContext)

    /** Updates the bounding box according to the current segment points.*/
    fun updateBoundingBox()
}