package ch.scorpion.jabbah.graph.ui.desktop

import kotlin.math.min

/**
 * Organizes the [GraphDesktopViewItem GraphDesktopViewItems] of a [DockingGraphDesktopViewSwing]
 * into columns and rows, and provides update operations.
 */
internal class DockingGraphDesktopViewItems(
    private val maxRowsCount: Int
) {

    private val columns: MutableList<MutableList<GraphDesktopViewItem>> = mutableListOf()

    val columnsCount get() = columns.size

    val isEmpty get() = columns.isEmpty()

    val all: List<GraphDesktopViewItem> get() = columns.flatten()

    fun getRowsCount(column: Int) = columns[column].size

    fun getRows(column: Int): List<GraphDesktopViewItem> = columns[column]

    fun getColumnWidth(column: Int) = columns[column][0].layoutWidth

    fun getRowHeight(column: Int, row: Int) = columns[column][row].layoutHeight

    fun getCurrentLocationOf(item: GraphDesktopViewItem): CurrentDockingLocation {
        val column = columns.indexOfFirst { it.contains(item) }
        val row = columns[column].indexOfFirst { it === item }
        return CurrentDockingLocation(column, row)
    }

    fun getItem(column: Int, row: Int): GraphDesktopViewItem = columns[column][row]

    fun addMainItem(item: GraphDesktopViewItem) {
        columns.clear()
        columns.add(mutableListOf(item))
    }

    fun addChildItem(item: GraphDesktopViewItem) {
        if (columns.size == 1) {
            // If there is already one column, always add the next one to a new second column
            columns.add(mutableListOf(item))

        } else {
            // Existing child columns
            if (columns.last().size < maxRowsCount) {
                columns.last().add(item)
            } else {
                columns.add(mutableListOf(item))
            }
        }
    }

    fun remove(item: GraphDesktopViewItem) {
        val column = columns.firstOrNull { it.contains(item) }
        if (column == null) {
            return
        }

        if (column.size == 1) {
            columns.remove(column)
        } else {
            column.remove(item)
        }
    }

    fun clear() {
        columns.clear()
    }

    fun move(item: GraphDesktopViewItem, target: NewDockingLocation) {
        val sourceColumnIndex = columns.indexOfFirst { it.contains(item) }
        if (sourceColumnIndex < 0) {
            return
        }
        val removeColumn = columns[sourceColumnIndex].size <= 1

        val sourceRowIndex = columns[sourceColumnIndex].indexOf(item)
        if (sourceRowIndex < 0) {
            return
        }

        // Calculate the effective indexes to be used AFTER the item has been removed.
        // These might be smaller than the target indexes which are based on the situation BEFORE removing the item.

        val effTargetColumnIndex = if (removeColumn && sourceColumnIndex < target.column.index) {
            target.column.index - 1
        } else {
            target.column.index
        }
        val effTargetRowIndex = if (sourceColumnIndex == target.column.index && sourceRowIndex < target.row.index) {
            target.row.index - 1
        } else {
            target.row.index
        }

        // Remove the item from its old postion
        remove(item)

        // Add the item to its new position
        val targetColumn = if (!target.column.insert && effTargetColumnIndex < columns.size) {
            columns[effTargetColumnIndex]
        } else {
            mutableListOf(item).also { columns.add(min(effTargetColumnIndex, columnsCount), it) }
        }
        if (!targetColumn.contains(item)) {
            if (effTargetRowIndex < targetColumn.size) {
                targetColumn.add(effTargetRowIndex, item)
            } else {
                targetColumn.add(item)
            }
        }
    }
}