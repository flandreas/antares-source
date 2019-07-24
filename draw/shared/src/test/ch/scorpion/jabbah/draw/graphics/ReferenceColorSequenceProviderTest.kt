package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReferenceColorSequenceProviderTest {

	companion object {
		private val WHITE = CompositeColor(Color.WHITE)
		private val RED = CompositeColor(Color.RED)
		private val YELLOW = CompositeColor(Color.YELLOW)

		private val BLUE = CompositeColor(Color.BLUE)
		private val BLACK = CompositeColor(Color.BLACK)
		private val GRAY = CompositeColor(Color.GRAY)
	}

	@BeforeTest
	fun beforeTest() {
		DrawTestRule.configure()
		ReferenceColorSequenceProvider.clear()
	}

	@Test
	fun shouldSetInitialColors() {
		ReferenceColorSequenceProvider.replaceColors(listOf(WHITE, RED, YELLOW))
		assertEquals(3, ReferenceColorSequenceProvider.colorCount)
		assertEquals(YELLOW, ReferenceColorSequenceProvider.getColor(2))
	}

	@Test
	fun shouldProvideColors() {
		ReferenceColorSequenceProvider.replaceColors(listOf(WHITE, RED, YELLOW))
		val sequence = ReferenceColorSequenceProvider.provide()

		assertEquals(WHITE, sequence.next())
		assertEquals(RED, sequence.next())
		assertEquals(YELLOW, sequence.next())
	}

	@Test
	fun shouldReplaceColors() {
		ReferenceColorSequenceProvider.replaceColors(listOf(WHITE, RED, YELLOW))
		ReferenceColorSequenceProvider.replaceColors(listOf(BLUE, BLACK, GRAY))

		assertEquals(3, ReferenceColorSequenceProvider.colorCount)
		assertEquals(BLUE, ReferenceColorSequenceProvider.getColor(0))
		assertEquals(BLACK, ReferenceColorSequenceProvider.getColor(1))
		assertEquals(GRAY, ReferenceColorSequenceProvider.getColor(2))
	}

	@Test
	fun shouldNotifyReplacement() {
		ReferenceColorSequenceProvider.replaceColors(listOf(WHITE, RED, YELLOW))

		val eventBus = mockk<EventBus>()
		val slot = slot<ReferenceColorEvent>()
		every { eventBus.post(capture(slot)) } answers { Unit }
		BaseModule.eventBus = eventBus

		ReferenceColorSequenceProvider.replaceColors(listOf(BLUE, BLACK, GRAY))
		val event = slot.captured

		assertEquals(BLUE, event.getNewColorFor(WHITE))
		assertEquals(BLACK, event.getNewColorFor(RED))
		assertEquals(GRAY, event.getNewColorFor(YELLOW))
	}
}