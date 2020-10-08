package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.style.Style
import ch.scorpion.jabbah.draw.style.DrawTheme

/**
 * An [Icon] is a rectangular graphical object that used methods of [Graphics2D] for drawing its representation.
 * In contrast to [Image], which represents bitmap data that doesn't zoom nice, [Icon]s are used for
 * displaying small graphical information within a [View].
 *
 * [Icons][Icon] usually don't have their own color property. The draw themselves in the foreground [Color]
 * of the [DrawTheme.figure] [Style]. In order for clients of [Icon] to implement things like hover or
 * disabling, which might involve rendering of the [Icon] in a derived [Color], [Icon] implementations
 * should respect [DrawContext.color] by using the [DrawContext.choose] method.
 *
 * To also support [Icons][Icon] which do have a [Color] property, clients of [Icon] must not set the context
 * [CompositeColor] if the color is NOT to be derived for hovering or disabling.
 */
interface Icon {

    val dim: Dimension2D

    /**
     * Draws this [Icon] using the specified [DrawContext].
     * @param location the location of the upper-left corner relative to the current origin of [context].
     */
    fun draw(context: DrawContext, location: Point2D)
}