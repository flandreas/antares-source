package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.model.LogEvent
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class LogEventHistoryTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val history = LogEventHistory()

	@Test
	fun shouldInitiallyHaveNoEventColumns() {
		assertEquals(0, history.eventColumnsCount)
	}

	@Test
	fun shouldAddEventsFromSameSourceToSameColumn() {
		val source = mockk<Vertice>()
		history.add(LogEvent(source, "A", "Value 1", 1))
		history.add(LogEvent(source, "A", "Value 2", 2))

		assertEquals(1, history.eventColumnsCount)
		assertEquals(2, history.rowsCount)
	}

	@Test
	fun shouldAddEventFromDifferentSourceToNewColumn() {
		val sourceA = mockk<Vertice>()
		val sourceB = mockk<Vertice>()
		history.add(LogEvent(sourceA, "A", "Value 1", 1))
		history.add(LogEvent(sourceB, "B", "Value 2", 2))

		assertEquals(2, history.eventColumnsCount)
		assertEquals(2, history.rowsCount)
	}

	@Test
	fun shouldCollectTimeOnRow() {
		val sourceA = mockk<Vertice>()
		val sourceB = mockk<Vertice>()
		history.add(LogEvent(sourceA, "A", "Value 1", 1))
		history.add(LogEvent(sourceB, "B", "Value 2", 1))

		assertEquals(2, history.eventColumnsCount)
		assertEquals(1, history.rowsCount)
	}

	@Test
	fun shouldReturnMostRecentValue() {
		val sourceA = mockk<Vertice>()
		val sourceB = mockk<Vertice>()
		history.add(LogEvent(sourceA, "A", "Value 1", 1))
		history.add(LogEvent(sourceB, "B", "Value 2", 2))

		assertEquals("Value 1", history.getValue(0, 0))
		assertEquals("", history.getValue(0, 1))
		assertEquals("Value 1", history.getValue(1, 0))
		assertEquals("Value 2", history.getValue(1, 1))
	}
}