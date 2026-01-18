package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Unit tests for [SignalHistory]. */
class SignalHistoryTest {

	private var history = SignalHistory<Int>(100)

	init {
		BaseModule.require()
	}

	@Test
	fun shouldNotAddSameSignal() {
		history.add(1, 100)
		history.add(1, 200)
		assertEquals(1, history.size)
	}

	@Test
	fun shouldAddChangingSignal() {
		history.add(1, 100)
		history.add(2, 200)
		history.add(1, 300)
		assertEquals(3, history.size)
	}

	@Test
	fun shouldTruncate() {
		history = SignalHistory(1)
		history.add(1, 0)
		history.add(2, 100)
		history.add(1, 200)

		assertEquals(1, history.size)
		assertEquals(200L, history.lastOrNull()!!.time)
	}

	@Test
	fun shouldYieldMinimum() {
		history.add(1, 100)
		history.add(2, 200)
		assertEquals(1, history.minimum)
		assertEquals(2, history.maximum)
	}

	@Test
	fun shouldNotKeepMinMaxFromBufferOverflow() {
		history = SignalHistory(2)
		history.add(10, 100)
		history.add(-10, 200)

		history.add(5, 300)
		assertEquals(5, history.maximum)

		history.add(0, 400)
		assertEquals(0, history.minimum)
	}

	@Test
	fun shouldYieldEntry() {
		history.add(1, 100)
		history.add(10, 200)
		history.add(5, 300)

		assertNull(history.getEntryAt(0)?.signal)
		assertEquals(1, history.getEntryAt(100)!!.signal)
		assertEquals(1, history.getEntryAt(110)!!.signal)
		assertEquals(10, history.getEntryAt(200)!!.signal)
		assertEquals(10, history.getEntryAt(250)!!.signal)
		assertEquals(5, history.getEntryAt(400)?.signal)
	}
}