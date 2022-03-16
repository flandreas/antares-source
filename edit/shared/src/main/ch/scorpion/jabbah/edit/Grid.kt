package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.edit.snap.DottedGridPainter
import ch.scorpion.jabbah.edit.snap.LineGridPainter

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

	    /** The name of the snap enabled [Boolean] property in [Properties].*/
	    const val PROP_SNAP_ENABLED = "edit.grid.snapEnabled"

	    /** The name of the [String] property in [Properties] that contains the name of the [GridPainter] to use.*/
	    const val PROP_GRID_PAINTER = "edit.grid.painter"
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

	/** Paints the visual representation of this [Grid].*/
	var gridPainter: GridPainter
}

enum class GridType(
	val id: String,
	val nameKey: String
) : EnumProperty<GridType> {

	Line(LineGridPainter.NAME, "edit.preferences.Grid.painter.line"),
	Dot(DottedGridPainter.NAME,"edit.preferences.Grid.painter.dot");

	companion object {
		fun withId(id: String): GridType = values().first { it.id == id }
	}

	override val customName: String get() = id

	override fun toString(): String = Translations.getString(nameKey)
}