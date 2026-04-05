package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.style.Style
import io.antarescircuit.jabbah.draw.style.DrawTheme
import io.antarescircuit.jabbah.draw.style.Themes

/**
 * An [Icon] is a rectangular graphical object that used methods of [Graphics2D] for drawing its representation.
 * In contrast to [Image], which represents bitmap data that doesn't zoom nicely, [Icon]s are used for
 * displaying small graphical information within a [View].
 */
interface Icon {

    val dim: Dimension2D

    /**
     * Draws this [Icon] in the colors defined by the [DrawTheme.figure] [Style].
     */
    fun draw(context: DrawContext, location: Point2D) =
        draw(context, location, context.choose(Themes.get<DrawTheme>().figure.color))

    /**
     * Draws this [Icon] using the specified [DrawContext].
     * @param location the location of the upper-left corner relative to the current origin of [context].
     * @param color the color in which this [Icon] is drawn
     */
    fun draw(context: DrawContext, location: Point2D, color: CompositeColor)
}