package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.signal.BitWidth.*
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.model.OutputPort
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class DigitalCombinedAccessTest {

	private val port = mockk<OutputPort<DigitalSignal>>()

	@Test
	fun shouldAttachLowerHalf() {
		val attached = DigitalCombinedNetAccess(port, BW_2, 0)
			.attach(port, DigitalCombinedNetAccess(port, BW_1, 0))

		assertEquals(BW_1, attached.width)
		assertEquals(0, attached.index)
	}

	@Test
	fun shouldAttachUpperHalf() {
		val attached = DigitalCombinedNetAccess(port, BW_2, 0)
			.attach(port, DigitalCombinedNetAccess(port, BW_1, 1))

		assertEquals(BW_1, attached.width)
		assertEquals(1, attached.index)
	}

	@Test
	fun shouldAttach1to2() {
		assertEquals(
			DigitalCombinedNetAccess(port, BW_1, 0),
			DigitalCombinedNetAccess(port, BW_1, 0).attach(port, DigitalCombinedNetAccess(port, BW_1, 0)))
		assertEquals(
			DigitalCombinedNetAccess(port, BW_1, 1),
			DigitalCombinedNetAccess(port, BW_1, 1).attach(port, DigitalCombinedNetAccess(port, BW_1, 0)))
	}

	@Test
	fun shouldAttach1to4() {
		assertEquals(
			DigitalCombinedNetAccess(port, BW_1, 0),
			DigitalCombinedNetAccess(port, BW_2, 0).attach(port, DigitalCombinedNetAccess(port, BW_1, 0)))
		assertEquals(
			DigitalCombinedNetAccess(port, BW_1, 1),
			DigitalCombinedNetAccess(port, BW_2, 0).attach(port, DigitalCombinedNetAccess(port, BW_1, 1)))
		assertEquals(
			DigitalCombinedNetAccess(port, BW_1, 2),
			DigitalCombinedNetAccess(port, BW_2, 1).attach(port, DigitalCombinedNetAccess(port, BW_1, 0)))
		assertEquals(
			DigitalCombinedNetAccess(port, BW_1, 3),
			DigitalCombinedNetAccess(port, BW_2, 1).attach(port, DigitalCombinedNetAccess(port, BW_1, 1)))
	}

	@Test
	fun shouldAttach2to4() {
		assertEquals(
			DigitalCombinedNetAccess(port, BW_2, 0),
			DigitalCombinedNetAccess(port, BW_2, 0).attach(port, DigitalCombinedNetAccess(port, BW_2, 0)))
		assertEquals(
			DigitalCombinedNetAccess(port, BW_2, 1),
			DigitalCombinedNetAccess(port, BW_2, 1).attach(port, DigitalCombinedNetAccess(port, BW_2, 0)))
	}
}