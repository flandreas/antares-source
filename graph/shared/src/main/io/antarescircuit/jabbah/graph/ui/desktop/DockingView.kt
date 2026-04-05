package io.antarescircuit.jabbah.graph.ui.desktop

import io.antarescircuit.jabbah.base.ui.UIView

interface DockingView : UIView {

    val viewWidth: Int
    val viewHeight: Int

    val columnsCount: Int

    fun getRowsCount(column: Int): Int

    fun getColumnWidth(column: Int): Int

    fun getRowHeight(column: Int, row: Int): Int
}