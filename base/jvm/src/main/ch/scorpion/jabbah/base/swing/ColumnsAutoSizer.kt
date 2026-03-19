
package ch.scorpion.jabbah.base.swing

import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import kotlin.math.max

/**
 * Optimizes the width of a [JTable]'s column to the width of largest cell content.
 * Source: https://bosmeeuw.wordpress.com/2011/08/07/java-swing-automatically-resize-table-columns-to-their-contents/
 */
object ColumnsAutoSizer {

    private val DEF_CELL_RENDERER = DefaultTableCellRenderer()

    fun sizeColumnsToFit(table: JTable, columnMargin: Int = 5) {
        val tableHeader = table.tableHeader

        if (tableHeader == null) {
            // can’t auto size a table without a header
            return
        }

        val headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont())
        val minWidths = Array(table.columnCount) { 0 }
        val maxWidths = Array(table.columnCount) { 0 }

        for (columnIndex in 0 until table.columnCount) {
            val headerWidth = headerFontMetrics.stringWidth(table.getColumnName(columnIndex))
            minWidths[columnIndex] = headerWidth + columnMargin
            val maxWidth = getMaximalRequiredColumnWidth(table, columnIndex, headerWidth)
            maxWidths[columnIndex] = max(maxWidth, minWidths[columnIndex]) + columnMargin
        }

        adjustMaximumWidths(table, minWidths, maxWidths)

        for (i in 0 until minWidths.size) {
            if (minWidths[i] > 0) {
                table.columnModel.getColumn(i).minWidth = minWidths[i]
            }
            if (maxWidths[i] > 0) {
                table.columnModel.getColumn(i).preferredWidth = maxWidths[i]
            }
        }
    }

    private fun adjustMaximumWidths(table: JTable, minWidths: Array<Int>, maxWidths: Array<Int>) {
        if (table.width > 0) {
            var breaker = 0

            while (maxWidths.sum() > table.width && breaker < 10000) {
                val highestWidthIndex = findLargestIndex(maxWidths)
                maxWidths[highestWidthIndex] -= 1
                maxWidths[highestWidthIndex] = max(maxWidths[highestWidthIndex], minWidths[highestWidthIndex])
                breaker ++
            }
        }
    }

    private fun getMaximalRequiredColumnWidth(table: JTable, columnIndex: Int, headerWidth: Int): Int {
        var maxWidth = headerWidth

        val column = table.columnModel.getColumn(columnIndex)
        val cellRenderer = column.cellRenderer ?: DEF_CELL_RENDERER

        for (row in 0 until table.rowCount) {
            val rendererComponent = cellRenderer.getTableCellRendererComponent(
                table,
                table.model.getValueAt(row, columnIndex),
                false,
                false,
                row,
                columnIndex
            )
            val valueWidth = rendererComponent.preferredSize.width
            maxWidth = max(maxWidth, valueWidth)
        }

        return maxWidth
    }

    private fun findLargestIndex(widths: Array<Int>): Int {
        var largestIndex = 0
        var largestValue = 0

        for (i in 0 until widths.size) {
            if (widths[i] > largestValue) {
                largestValue = i
                largestIndex = widths[i]
            }
        }

        return largestIndex
    }
}
