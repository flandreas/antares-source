package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.ui.AbstractUIController

data class CurrentDockingLocation(
    val column: Int,
    val row: Int
)

data class NewDockingLocationCell(
    var index: Int,
    var insert: Boolean
)

/**
 * Represents the suggestion where a dragged view would be placed.
 * @property area the size and position the dragged view's suggested new area
 */
data class NewDockingLocation(
    val column: NewDockingLocationCell = NewDockingLocationCell(0, false),
    val row: NewDockingLocationCell = NewDockingLocationCell(0, false),
    val area: Rectangle2D = Rectangle2D()
) {
    companion object {

        fun newRow(column: Int, row: Int): NewDockingLocation =
            NewDockingLocation(
                NewDockingLocationCell(column, false),
                NewDockingLocationCell(row, true)
            )

        fun newColumn(column: Int): NewDockingLocation =
            NewDockingLocation(
                NewDockingLocationCell(column, true),
                NewDockingLocationCell(0, true)
            )
    }
}

class DockingController : AbstractUIController<DockingView>() {

    companion object {
        private val LOG by logger(DockingController::class)

        private const val SENSITIVE_COL_WIDTH = 1.0 / 6.0
    }

    var startLocation: CurrentDockingLocation? = null
        private set

    /**
     * Fly-weight object to return results in [mouseDragged]. Avoids creation of lots of objects
     * during mouse dragging.
     */
    private val newDockingLocation = NewDockingLocation()

    /**
     * Fly-weight object to return results in [getBounds]. Avoids creation of lots of objects
     * during mouse dragging.
     */
    private val currentLocationBounds = Rectangle2D()

    fun getBounds(loc: CurrentDockingLocation): RectangularShape {
        var x = 0
        for (column in 0 until loc.column) {
            x += view.getColumnWidth(column)
        }
        var y = 0
        for (row in 0 until loc.row) {
            y += view.getRowHeight(loc.column, row)
        }
        currentLocationBounds.setFrame(x, y, view.getColumnWidth(loc.column), view.getRowHeight(loc.column, loc.row))
        return currentLocationBounds
    }

    fun startDragging(location: CurrentDockingLocation) {
        this.startLocation = location
    }

    fun mouseDragged(mx: Int, my: Int): NewDockingLocation? {
        if (startLocation == null) {
            return null
        }
        val bounds = getBounds(startLocation!!)
        if (bounds.contains(mx, my)) {
            with(newDockingLocation) {
                column.index = startLocation!!.column
                column.insert = false
                row.index = startLocation!!.row
                row.insert = false
                area.setFrame(bounds)
            }
            return newDockingLocation
        }

        // Determine column
        var areaX = 0
        var areaWidth = 0
        var locatedColumnIndex = -1
        var newColumnIndex = -1
        var newColumnInsert = false
        var x = 0
        for (column in 0 until view.columnsCount) {
            val columnWidth = view.getColumnWidth(column)
            if (mx < x + columnWidth * SENSITIVE_COL_WIDTH) {
                // Left sensitive region of column
                locatedColumnIndex = column
                newColumnIndex = column
                newColumnInsert = true
                areaX = x
                areaWidth = columnWidth / 2
                break
            } else if (mx <= x + columnWidth * (1 - SENSITIVE_COL_WIDTH)) {
                // Middle of column
                locatedColumnIndex = column
                newColumnIndex = column
                newColumnInsert = false
                areaX = x
                areaWidth = columnWidth
                break
            } else if (mx <= x + columnWidth) {
                // Right sensitive region of column
                locatedColumnIndex = column
                newColumnIndex = column + 1
                newColumnInsert = true
                areaX = x + columnWidth / 2
                areaWidth = columnWidth / 2
                break
            }
            x += columnWidth
            areaX = x
        }
        if (newColumnIndex < 0 || locatedColumnIndex < 0) {
            // This shouldn't happen, something is wrong
            LOG.debug("Unexpected column location")
            return null
        }

        // Determine row
        var areaY = 0
        var areaHeight = 0
        var newRowIndex = -1
        var newRowInsert = false

        if (newColumnInsert) {
            newRowIndex = 0
            newRowInsert = true
            areaHeight = view.viewHeight
        } else {
            var y = 0
            for (row in 0 until view.getRowsCount(locatedColumnIndex)) {
                val rowHeight = view.getRowHeight(locatedColumnIndex, row)
                if (my <= y + rowHeight / 2) {
                    // Top half of row
                    newRowIndex = row
                    newRowInsert = true
                    areaY = y
                    areaHeight = rowHeight / 2
                    break
                } else if (my <= y + rowHeight) {
                    // Bottom half of row
                    newRowIndex = row + 1
                    newRowInsert = true
                    areaY = y + rowHeight / 2
                    areaHeight = rowHeight / 2
                    break
                }
                y += rowHeight
                areaY = y
            }
        }
        if (newRowIndex < 0) {
            // This shouldn't happen, something is wrong
            LOG.debug("Unexpected row location $newRowIndex in column $locatedColumnIndex")
            return null
        }

        with(newDockingLocation) {
            column.index = newColumnIndex
            column.insert = newColumnInsert
            row.index = newRowIndex
            row.insert = newRowInsert
            area.setFrame(areaX, areaY, areaWidth, areaHeight)
        }

        return newDockingLocation
    }
}