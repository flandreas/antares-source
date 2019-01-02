package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.View

/**
 * A [Grid] defines a two-dimensional array of points that are used for snapping in terms of a [Snapper].
 */
interface Grid : Snapper, Unzoomable {

    companion object {

        /** The name of the default grid distance [Int] property in [Properties]. */
        const val PROP_GRID_DEFAULT_DISTANCE = "edit.grid.distance.default"

        /** The name of the minimal grid distance [Int] property in [Properties]. */
        const val PROP_GRID_MIN_DISTANCE = "edit.grid.distance.min"

        /** The name of the default paint factor [Int] property in [Properties]. */
        const val PROP_GRID_DEFAULT_PAINT_FACTOR = "edit.grid.paintFactor.default"

	    /** The name of the snap enabed [Boolean] property in [Properties].*/
	    const val PROP_SNAP_ENABLED = "edit.grid.snapEnabled"
    }

    /** The [View] to which this [Grid] belongs. Only `null` until initialized. */
    var view: View<EditInputEventContext>?

    /** The distance between two grid points in model space.*/
    var distance: Double

    /**
     * The number of snapping points between two painted dots. For example, if this value is 2, only
     * every second dot is painted.
     */
    var paintFactor: Int


}