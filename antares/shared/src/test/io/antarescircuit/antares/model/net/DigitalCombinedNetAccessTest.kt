package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_1
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_2
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_4
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_8
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.graph.model.OutputPort
import dev.mokkery.mock
import kotlin.test.*

class DigitalCombinedNetAccessTest {

	private val port = mock<OutputPort<DigitalSignal>>()

	@Test
	fun shouldContainQuarterAccess() {
		val lowerAccess = DigitalCombinedNetAccess(port, BW_4, 0)
		assertTrue(lowerAccess.contains(DigitalCombinedNetAccess(port, BW_1, 0)))
		assertTrue(lowerAccess.contains(DigitalCombinedNetAccess(port, BW_1, 3)))

		val upperAccess = DigitalCombinedNetAccess(port, BW_4, 1)
		assertTrue(upperAccess.contains(DigitalCombinedNetAccess(port, BW_1, 4)))
		assertTrue(upperAccess.contains(DigitalCombinedNetAccess(port, BW_1, 7)))
	}

	@Test
	fun shouldContainHalfAccess() {
		val lowerAccess = DigitalCombinedNetAccess(port, BW_4, 0)
		assertTrue(lowerAccess.contains(DigitalCombinedNetAccess(port, BW_2, 0)))
		assertTrue(lowerAccess.contains(DigitalCombinedNetAccess(port, BW_2, 1)))
	}

	@Test
	fun shouldContainFullAccess() {
		val lowerAccess = DigitalCombinedNetAccess(port, BW_4, 0)
		assertTrue(lowerAccess.contains(DigitalCombinedNetAccess(port, BW_4, 0)))
	}

	@Test
	fun shouldNotContainQuarterAccess() {
		val lowerAccess = DigitalCombinedNetAccess(port, BW_4, 0)
		assertFalse(lowerAccess.contains(DigitalCombinedNetAccess(port, BW_1, 4)))

		val upperAccess = DigitalCombinedNetAccess(port, BW_4, 1)
		assertFalse(upperAccess.contains(DigitalCombinedNetAccess(port, BW_1, 3)))
		assertFalse(upperAccess.contains(DigitalCombinedNetAccess(port, BW_1, 8)))
	}

	@Test
	fun shouldNotContainHalfAccess() {
		val lowerAccess = DigitalCombinedNetAccess(port, BW_4, 0)
		assertFalse(lowerAccess.contains(DigitalCombinedNetAccess(port, BW_2, 2)))
	}

	@Test
	fun shouldNotContainFullAccess() {
		val lowerAccess = DigitalCombinedNetAccess(port, BW_4, 0)
		assertFalse(lowerAccess.contains(DigitalCombinedNetAccess(port, BW_4, 1)))
	}

	@Test
	fun shouldNotContainLargerAccess() {
		val lowerAccess = DigitalCombinedNetAccess(port, BW_4, 0)
		assertFalse(lowerAccess.contains(DigitalCombinedNetAccess(port, BW_8, 0)))
		assertFalse(lowerAccess.contains(DigitalCombinedNetAccess(port, BW_8, 1)))
	}
}