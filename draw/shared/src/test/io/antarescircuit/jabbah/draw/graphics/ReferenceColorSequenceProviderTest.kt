package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawTestRule
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReferenceColorSequenceProviderTest {

	companion object {
		private val WHITE = ReferenceColor(CompositeColor(Color.WHITE))
		private val RED = ReferenceColor(CompositeColor(Color.RED))
		private val YELLOW = ReferenceColor(CompositeColor(Color.YELLOW))

		private val BLUE = ReferenceColor(CompositeColor(Color.BLUE))
		private val BLACK = ReferenceColor(CompositeColor(Color.BLACK))
		private val GRAY = ReferenceColor(CompositeColor(Color.GRAY))
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

		val eventBus = mock<EventBus>(MockMode.autofill)
		val slot = Capture.slot<ReferenceColorEvent>()
		every { eventBus.post(capture(slot)) } returns Unit
		BaseModule.eventBus = eventBus

		ReferenceColorSequenceProvider.replaceColors(listOf(BLUE, BLACK, GRAY))
		val event = slot.get()

		assertEquals(BLUE, event.getNewColorFor(WHITE))
		assertEquals(BLACK, event.getNewColorFor(RED))
		assertEquals(GRAY, event.getNewColorFor(YELLOW))
	}
}