package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [SignalHistory]. */
class SignalHistoryImplTest {

	companion object {
		init {
			BaseModule.require()
		}
	}

	private val history = SignalHistoryImpl<Boolean>()

	@Test
	fun shouldNotAddSameSignal() {
		history.add(false, 100)
		history.add(false, 200)
		assertEquals(1, history.size)
	}

	@Test
	fun shouldAddChangingSignal() {
		history.add(false, 100)
		history.add(true, 200)
		history.add(false, 300)
		assertEquals(3, history.size)
	}

	@Test
	fun shouldTruncate() {
		history.add(true, 0)
		history.add(false, 100)
		history.add(true, 200)
		history.truncate(200)

		assertEquals(1, history.size)
		assertEquals(200L, history.lastOrNull()!!.time)
	}
}