package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.RectangularShape
import kotlin.math.ceil

/**
 * Defines properties and methods common to all rectangular graphical objects.
 */
interface RectangularDrawable : Locatable {

    val bounds: RectangularShape

    var width: Double

    val widthInt: Int get() = ceil(width).toInt()

    var height: Double

    val heightInt: Int get() = ceil(height).toInt()

    val x: Double get() = bounds.x

    val y: Double get() = bounds.y

    val xInt: Int get() = x.toInt()

    val yInt: Int get() = y.toInt()

    val lineWidth: Double

    fun setBounds(x: Double, y: Double, w: Double, h: Double)

    fun setBounds(x: Int, y: Int, w: Int, h: Int) = setBounds(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())

    fun setBounds(r: RectangularShape) = setBounds(r.x, r.y, r.width, r.height)

    fun contains(x: Double, y: Double, w: Double, h: Double): Boolean
}