package io.antarescircuit.jabbah.graph.view.net.netview

import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewStyling

interface NetViewStyling {

    val boundingBox: RectangularShape

    /** Returns `true` is this [NetViewStyling] draws large or wide areas, which is used for determining background colors. */
    val isArea: Boolean

    fun updateBoundingBox()

    /**
     * Draws this [EdgeViewStyling].
     * This method expects that the caller passes the [CompositeColor] to be used for drawing in the provided
     * [DrawContext], i.e. this method uses the color in [DrawContext.color] for drawing its foreground
     * and its background. The same applies for the [Stroke].
     */
    fun draw(context: DrawContext)
}