package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.Settings
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.drawable.RichTextDrawable
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.draw.richtext.RichTextTableCellRenderer
import io.antarescircuit.jabbah.graph.ui.logview.LogView
import io.antarescircuit.jabbah.graph.ui.logview.LogViewController
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/**
 * A [javax.swing] implementation of [LogView]
 */
class LogViewSwing(
	controller: LogViewController,
	private val settings: Settings = BaseModule.settings
) : JPanel(), LogView {

	companion object {
		private const val SETTING_COLUMN_WIDTHS = "graph.logPanel.columnWidth"
		private val TIME_COLUMN_NAME = Translations.getString("log.property.time.name")
	}

	/** Owned by the [LogViewController]. Only used for reading. */
	private val eventHistory = controller.logEventHistory

	private val rightAlignedRenderer = DefaultTableCellRenderer()

	private val table = JTable(LogEventTableModel())

	private var richTextColumnNames: List<RichTextDrawable> = emptyList()

	init {
		controller.view = this
		rightAlignedRenderer.horizontalAlignment = JLabel.RIGHT
		createRichTextColumnNames()
		buildUI()
	}

	override fun dispose() {
		storeColumnWidths()
	}

	override fun refresh(oldColumnsCount: Int) {
		if (oldColumnsCount != eventHistory.eventColumnsCount) {
			storeColumnWidths()
			(table.model as LogEventTableModel).fireTableStructureChanged()
			for (index in 0 until table.columnModel.columnCount) {
				table.columnModel.getColumn(index).cellRenderer = rightAlignedRenderer
			}
			setColumnWidths()
			createRichTextColumnNames()
		}

		(table.model as LogEventTableModel).fireTableDataChanged()
		table.scrollRectToVisible(table.getCellRect(eventHistory.rowsCount - 1, 0, true))
	}

	private fun createRichTextColumnNames() {
		val columNames = mutableListOf<RichTextDrawable>()
		val font = Graphics2DJvm.fromAwtFont(table.font)
		for (index in 0 until table.columnModel.columnCount) {
			columNames.add(RichTextDrawable.of((table.model as LogEventTableModel).getColumnName(index), font))
		}
		richTextColumnNames = columNames
	}

	private fun buildUI() {
		table.autoResizeMode = JTable.AUTO_RESIZE_OFF
		table.tableHeader.defaultRenderer = TableColumnRenderer()
		layout = BorderLayout()
		val scrollPane = JScrollPane(table)
		add(scrollPane, BorderLayout.CENTER)
		setColumnWidths()
	}

	private fun storeColumnWidths() {
		settings.set("$SETTING_COLUMN_WIDTHS.time", table.columnModel.getColumn(0).width)
	}

	private fun setColumnWidths() {
		table.columnModel.getColumn(0).preferredWidth = settings.getInt("$SETTING_COLUMN_WIDTHS.time", 120)
	}

	private inner class LogEventTableModel : AbstractTableModel() {

		override fun getColumnName(column: Int): String {
			return if (column == 0) {
				TIME_COLUMN_NAME
			} else {
				eventHistory.getEventColumnName(column - 1)
			}
		}

		override fun getRowCount(): Int = eventHistory.rowsCount

		override fun getColumnCount(): Int = 1 + eventHistory.eventColumnsCount

		override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
			return when(columnIndex) {
				0 -> StringUtils.formatLong(eventHistory.getTime(rowIndex))
				else -> eventHistory.getValue(rowIndex, columnIndex - 1)
			}
		}
	}

	private inner class TableColumnRenderer : RichTextTableCellRenderer() {
		override fun getTableCellRendererComponent(
			table: JTable?,
			value: Any?,
			isSelected: Boolean,
			hasFocus: Boolean,
			row: Int,
			column: Int
		): Component? {
			val renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as RichTextTableCellRenderer
			renderer.horizontalAlignment = RIGHT
			renderer.richText = richTextColumnNames[column]
			return renderer
		}
	}
}

