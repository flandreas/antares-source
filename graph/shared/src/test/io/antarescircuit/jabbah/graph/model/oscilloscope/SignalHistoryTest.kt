package io.antarescircuit.jabbah.graph.model.oscilloscope

import kotlin.random.Random
import kotlin.test.*

class SignalHistoryTest {

	@Test
	fun shouldMatchRetainedChangesAfterRepeatedBufferWraps() {
		val history = SignalHistory<Int>(17)
		val reference = mutableListOf<SignalHistoryEntry<Int>>()
		val random = Random(42)
		repeat(2_000) { time ->
			val entry = SignalHistoryEntry(random.nextInt(10), time.toLong())
			history.add(entry)
			if (reference.lastOrNull()?.signal != entry.signal) {
				reference.add(entry)
				if (reference.size > 17) reference.removeAt(0)
			}
			assertEquals(reference.minOf { it.signal }, history.minimum)
			assertEquals(reference.maxOf { it.signal }, history.maximum)
			assertEquals(reference.last().time, history.maxTime)
			val query = random.nextLong(-1, time + 2L)
			assertEquals(reference.lastOrNull { it.time <= query }, history.getEntryAt(query))
			assertEquals(reference.filter { it.time >= query }, history.getEntriesSince(query).asSequence().toList())
			assertEquals(reference.filter { it.time >= query }.reversed(), history.getReverseEntriesUntil(query).asSequence().toList())
		}
		assertTrue(history.overflow)
	}

	@Test
	fun shouldUseLastChangeAtSameTimestampAndCompressUnchangedSignals() {
		val history = SignalHistory<Int>(4)
		history.add(0, 10)
		history.add(1, 10)
		history.add(1, 11)
		history.add(2, 20)
		assertEquals(3, history.size)
		assertNull(history.getEntryAt(9))
		assertEquals(1, history.getEntryAt(10)?.signal)
		assertEquals(1, history.getEntryAt(19)?.signal)
		assertEquals(2, history.getEntryAt(Long.MAX_VALUE)?.signal)
		assertEquals(listOf(0, 1, 2), history.getEntriesSince(10).asSequence().map { it.signal }.toList())
		assertFalse(history.getEntriesSince(21).hasNext())
		assertFailsWith<NoSuchElementException> { history.getReverseEntriesUntil(21).next() }
		assertFailsWith<IllegalArgumentException> { history.add(3, 19) }
	}

	@Test
	fun shouldHandleEmptyAndSingleEntryBuffersAndResetExtrema() {
		val disabled = SignalHistory<Int>(0)
		disabled.add(1, 0)
		assertTrue(disabled.isEmpty)
		assertTrue(disabled.overflow)
		assertFailsWith<IllegalArgumentException> { SignalHistory<Int>(-1) }
		val history = SignalHistory<Int>(1)
		history.add(1, 0)
		history.add(2, 1)
		assertEquals(2, history.minimum)
		assertEquals(2, history.maximum)
		assertEquals(1, history.size)
		history.clear()
		assertEquals(0, history.size)
		assertEquals(0L, history.maxTime)
		assertNull(history.minimum)
		assertNull(history.maximum)
		assertFalse(history.overflow)
		history.add(3, 0)
		assertEquals(3, history.last().signal)
	}

	@Test
	fun shouldKeepNonComparableSignalsWithoutExtrema() {
		val history = SignalHistory<List<Int>>(2)
		history.add(listOf(1), 0)
		history.add(listOf(2), 1)
		assertNull(history.minimum)
		assertNull(history.maximum)
		assertEquals(listOf(2), history.last().signal)
	}

	@Test
	fun shouldMaintainExtremaWithLinearTotalComparisons() {
		var comparisons = 0
		class Signal(val value: Int) : Comparable<Signal> {
			override fun compareTo(other: Signal): Int {
				comparisons++
				return value.compareTo(other.value)
			}
		}
		val history = SignalHistory<Signal>(1_000)
		repeat(5_000) { history.add(Signal(it), it.toLong()) }
		assertEquals(4_000, history.minimum?.value)
		assertEquals(4_999, history.maximum?.value)
		assertTrue(comparisons < 20_000, "Used $comparisons comparisons")
	}
}
