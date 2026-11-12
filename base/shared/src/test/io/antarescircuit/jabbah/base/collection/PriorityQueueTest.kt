package io.antarescircuit.jabbah.base.collection

import kotlin.random.Random
import kotlin.test.*

class PriorityQueueTest {

	@Test
	fun shouldReturnLeastElementWithoutRemovingIt() {
		val queue = PriorityQueue<Int>()
		listOf(10, 9, 11).forEach(queue::add)
		assertEquals(9, queue.peek())
		assertEquals(3, queue.size)
		assertEquals(listOf(9, 10, 11), List(3) { queue.remove() })
		assertNull(queue.peek())
		assertFailsWith<NoSuchElementException> { queue.remove() }
	}

	@Test
	fun shouldMatchSortedReferenceAcrossMixedOperations() {
		val random = Random(42)
		val queue = PriorityQueue<Int>()
		val reference = mutableListOf<Int>()
		repeat(5_000) {
			when (random.nextInt(4)) {
				0, 1 -> {
					val value = random.nextInt(100)
					queue.add(value)
					reference.add(value)
					reference.sort()
				}
				2 -> {
					val value = random.nextInt(100)
					queue.remove(value)
					reference.remove(value)
				}
				3 -> if (reference.isNotEmpty()) {
					assertEquals(reference.removeAt(0), queue.remove())
				}
			}
			assertEquals(reference.size, queue.size)
			assertEquals(reference.firstOrNull(), queue.peek())
		}
		assertEquals(reference, List(queue.size) { queue.remove() })
	}

	@Test
	fun shouldPreserveInsertionOrderForEqualPriorities() {
		val queue = PriorityQueue<Item>()
		val items = List(100) { Item(it % 4, it) }
		items.forEach(queue::add)
		assertEquals(items.sortedBy { it.priority }, List(items.size) { queue.remove() })
	}

	@Test
	fun shouldFindLeastMatchingElementWithoutRemovingIt() {
		val queue = PriorityQueue<Int>()
		listOf(7, 2, 9, 1, 4, 3).forEach(queue::add)
		assertEquals(3, queue.find { it % 2 == 1 && it > 1 })
		assertNull(queue.find { it > 9 })
		assertEquals(6, queue.size)
		assertEquals(1, queue.remove())
	}

	@Test
	fun shouldKeepSortedSnapshotIndependentOfSubsequentChanges() {
		val queue = PriorityQueue<Int>()
		listOf(3, 1, 2).forEach(queue::add)
		val snapshot = queue.elements()
		queue.remove()
		queue.add(0)
		queue.clear()
		assertEquals(listOf(1, 2, 3), snapshot)
		assertTrue(queue.isEmpty)
		queue.add(4)
		assertEquals(4, queue.remove())
	}

	@Test
	fun shouldUseLogarithmicComparisonsForInsertionAndHeadRemoval() {
		var comparisons = 0
		val queue = PriorityQueue<Item>()
		val size = 4_096
		for (i in size downTo 1) queue.add(Item(i, i) { comparisons++ })
		repeat(size) { assertEquals(it + 1, queue.remove().priority) }
		// A full sort on each insertion needs millions of comparisons for this workload.
		assertTrue(comparisons < size * 40, "Used $comparisons comparisons")
	}

	private data class Item(val priority: Int, val id: Int, val onCompare: () -> Unit = {}) : Comparable<Item> {
		override fun compareTo(other: Item): Int {
			onCompare()
			return priority.compareTo(other.priority)
		}
	}
}
