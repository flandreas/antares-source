package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.model.LogEvent
import ch.scorpion.jabbah.graph.model.Vertice
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/** Displays collected [LogEvent]s.*/
class LogPanel(
	private val eventBus: EventBus = BaseModule.eventBus,
	private val settings: Settings = BaseModule.settings
) : JPanel() {

	companion object {
		private const val SETTING_COLUMN_WIDTHS = "jabbah.graph.logPanel.columnWidth"
		private val TIME_COLUMN_NAME = Translations.getString("log.property.time.name")
	}

	private val rightAlignedRenderer = DefaultTableCellRenderer()

	private val eventHistory = LogEventHistory()

	private val table = JTable(LogEventTableModel())

	private val logEventHandler: EventHandler<LogEvent> = { handle(it) }

	private val activationHandler: EventHandler<SchedulerActivationStateEvent> = { handle(it) }

	init {
		rightAlignedRenderer.horizontalAlignment = JLabel.RIGHT
		eventBus.register(LogEvent::class, logEventHandler)
		eventBus.register(SchedulerActivationStateEvent::class, activationHandler)
		buildUI()
	}

	fun dispose() {
		eventBus.unregister(logEventHandler)
		eventBus.unregister(activationHandler)
	}

	private fun buildUI() {
		table.autoResizeMode = JTable.AUTO_RESIZE_OFF
		layout = BorderLayout()
		val scrollPane = JScrollPane(table)
		add(scrollPane, BorderLayout.CENTER)
	}

	fun clear() {
		eventHistory.clear()
		(table.model as LogEventTableModel).fireTableDataChanged()
	}

	private fun handle(event: LogEvent) {
		val oldColumnCount = eventHistory.eventColumnsCount
		eventHistory.add(event)
		if (oldColumnCount != eventHistory.eventColumnsCount) {
			(table.model as LogEventTableModel).fireTableStructureChanged()
			for (index in 0 until table.columnModel.columnCount) {
				table.columnModel.getColumn(index).cellRenderer = rightAlignedRenderer
			}
			table.columnModel.getColumn(0).preferredWidth = 120
		}

		(table.model as LogEventTableModel).fireTableDataChanged()
		table.scrollRectToVisible(table.getCellRect(eventHistory.rowsCount - 1, 0, true))
	}

	private fun handle(event: SchedulerActivationStateEvent) {
		if (event.scheduler.isActive) {
			clear()
		}
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
}

class LogEventHistory {

	private val eventColumns = mutableListOf<EventColumn>()

	private val eventRows = mutableListOf<EventRow>()

	val rowsCount: Int get() = eventRows.size

	val eventColumnsCount: Int get() = eventColumns.size

	fun add(event: LogEvent) {
		addColumnIfNecessary(event)
		addEvent(event)
	}

	private fun addEvent(event: LogEvent) {
		addRowIfNecessary(event)
		eventRows.last().add(eventColumns.find { it.source === event.source }!!, event.value)
	}

	private fun addColumnIfNecessary(event: LogEvent): Boolean {
		val column = eventColumns.find { it.source === event.source }
		if (column == null) {
			eventColumns.add(EventColumn(event.source, event.name))
			return true
		}
		return false
	}

	private fun addRowIfNecessary(event: LogEvent) {
		if (eventRows.isEmpty()) {
			eventRows.add(EventRow(event.time))
		} else if (eventRows.last().time != event.time) {
			eventRows.add(eventRows.last().copyForTime(event.time))
		}
	}

	fun clear() {
		eventColumns.clear()
		eventRows.clear()
	}

	fun getEventColumnName(index: Int): String = eventColumns[index].name

	fun getTime(row: Int): Long = eventRows[row].time

	fun getValue(row: Int, column: Int): String = eventRows[row].getValue(eventColumns[column])

	private data class EventColumn(val source: Vertice, val name: String)

	private class EventRow(val time: Long, entries: MutableMap<EventColumn,String> = mutableMapOf()) {

		/** Maps an [EventColumn] to the value of the [LogEvent] that has occurred at the given time. */
		private val entries = entries

		fun getValue(column: EventColumn): String = entries[column] ?: ""

		fun add(column: EventColumn, value: String) {
			entries[column] = value
		}

		fun copyForTime(time: Long): EventRow {
			return EventRow(time, entries.toMutableMap())
		}
	}
}
