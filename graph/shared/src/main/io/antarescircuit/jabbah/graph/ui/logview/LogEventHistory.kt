package io.antarescircuit.jabbah.graph.ui.logview

import io.antarescircuit.jabbah.graph.model.LogEvent
import io.antarescircuit.jabbah.graph.model.Vertice

/**
 * A view model of [LogEvent]s to be displayed in a [LogView].
 * Maintained and filled by a [LogViewController].
 */
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
		private val entries = entries.toMutableMap()

		fun getValue(column: EventColumn): String = entries[column] ?: ""

		fun add(column: EventColumn, value: String) {
			entries[column] = value
		}

		fun copyForTime(time: Long): EventRow {
			return EventRow(time, entries)
		}
	}
}