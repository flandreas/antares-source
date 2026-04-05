package io.antarescircuit.jabbah.graph.ui.desktop

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

class DockingViewMockBuilder(
    private val width: Int,
    private val height: Int
) {

    private val dockingView = mock<DockingView>()

    init {
        withWidth(width)
        withHeight(height)
    }

    /**
     * Creates a [DockingView] with equally spaced columns and rows.
     * @param columns the list of rows per column, e.g. `3,1` means two columns,
     * the first with 3 rows and the second with 1 row.
     */
    fun withColumnRows(vararg columns: Int): DockingViewMockBuilder {
        withColumnsCount(columns.size)
        columns.forEachIndexed { column, rowCount ->
            withColumnWidth(column, width / columns.size)
            withRowsCount(column, rowCount)
            for (row in 0 until rowCount) {
                withRowHeight(column, row, height / rowCount)
            }
        }
        return this
    }

    fun withWidth(width: Int): DockingViewMockBuilder {
        every { dockingView.viewWidth } returns width
        return this
    }

    fun withHeight(height: Int): DockingViewMockBuilder {
        every { dockingView.viewHeight } returns height
        return this
    }

    fun withColumnsCount(columnsCount: Int): DockingViewMockBuilder {
        every { dockingView.columnsCount } returns columnsCount
        return this
    }

    fun withRowsCount(column: Int, rowCount: Int): DockingViewMockBuilder {
        every { dockingView.getRowsCount(column) } returns rowCount
        return this
    }

    fun withColumnWidth(column: Int, width: Int): DockingViewMockBuilder {
        every { dockingView.getColumnWidth(column) } returns width
        return this
    }

    fun withRowHeight(column: Int, row: Int, height: Int): DockingViewMockBuilder {
        every { dockingView.getRowHeight(column, row) } returns height
        return this
    }

    fun build(): DockingView = dockingView
}