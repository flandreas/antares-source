package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * A [Handle] is a small [Drawable] that invites the user to manipulate it with the mouse in order to change
 * the shape of a [Component].
 *
 * [Handle]s are typically part of [SelectionModel]s.
 */
interface Handle : Drawable, Unzoomable {

    companion object {
        /** The name of the [Integer] property in [Properties] that designates half of the size of a handle.  */
        val PROP_SIZE_HALF = "select.handle.size_half"

        /** The name of the [Color] property in Properties that designates the [Color] of the handle's border.  */
        val PROP_BORDER_COLOR = "select.handle.color.border"

        /** The name of the [Color] property in Properties that designates the [Color] of the handle's content.  */
        val PROP_FILL_COLOR = "select.handle.color.content"

        /** The name of the [Stroke] property in Properties that designates the [Stroke] of the handle.  */
        val PROP_STROKE = "select.handle.stroke"
    }

    /** Holds the location of this [Handle], which is typically the center of the geometrical shape.*/
    var location: Point2D

    fun setLocation(x: Double, y: Double) {
        location.setLocation(x, y)
    }

}