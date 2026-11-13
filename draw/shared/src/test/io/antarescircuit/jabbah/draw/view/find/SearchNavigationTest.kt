package io.antarescircuit.jabbah.draw.view.find

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchNavigationTest {
	@Test
	fun shouldVisitAllMatchesAndWrapInBothDirections() {
		val navigation = SearchNavigation<String>()
		navigation.reset(listOf("a", "b", "c"))
		assertEquals(SearchResult(3), navigation.result)
		assertEquals("a", navigation.move(true))
		assertEquals(SearchResult(3, 1), navigation.result)
		assertEquals("c", navigation.move(false))
		assertEquals("a", navigation.move(true))
		assertEquals("b", navigation.move(true))
		navigation.reset(listOf("a", "b", "c"))
		assertEquals("c", navigation.move(false))
	}

	@Test
	fun shouldPreserveCursorWhenEarlierMatchesAreDeleted() {
		val navigation = SearchNavigation<String>()
		navigation.reset(listOf("a", "b", "c"))
		navigation.move(true)
		navigation.move(true)
		assertEquals("c", navigation.move(true) { it != "a" })
		assertEquals(SearchResult(2, 2), navigation.result)
		assertNull(navigation.move(true) { false })
		assertEquals(SearchResult(0), navigation.result)
	}

	@Test
	fun shouldResetForNewQueriesAndHandleEmptyResults() {
		val navigation = SearchNavigation<Int>()
		assertNull(navigation.move(false))
		navigation.reset(listOf(1))
		assertEquals(1, navigation.move(true))
		assertEquals(1, navigation.move(false))
		navigation.reset(emptyList())
		assertEquals(SearchResult(0), navigation.result)
		assertNull(navigation.move(true))
	}
}
