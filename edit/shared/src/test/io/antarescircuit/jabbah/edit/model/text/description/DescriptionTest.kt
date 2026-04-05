package io.antarescircuit.jabbah.edit.model.text.description

import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.text.Translation
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.io.StoreWriter
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.*

class DescriptionTest {

	@BeforeTest
	fun setup() {
		EditModule.require()
	}

	@Test
	fun shouldGetDescription() {
		val describable = TestDescribable("Test")
		assertEquals("Test", describable.description.value)
	}

	@Test
	fun shouldSetDescription() {
		val describable = TestDescribable("Test")
		describable.description = Description("Changed")
		assertEquals("Changed", describable.description.value)
	}

	@Test
	fun shouldPostDescriptionChangedEvent() {
		val describable = TestDescribable("Test")
		lateinit var event: DescriptionChangedEvent
		BaseModule.eventBus.register(DescriptionChangedEvent::class) { event = it }

		describable.description = Description("Changed")

		assertNotNull(event)
		assertSame(describable, event.owner)
		assertEquals("Test", event.oldValue.value)
		assertEquals("Changed", event.description.value)
	}

	@Test
	fun shouldInvokeChangeHandler() {
		val describable = TestDescribable("Test")
		assertFalse(describable.changed)

		describable.description = Description("Changed")

		assertTrue(describable.changed)
	}

	@Test
	fun shouldNotWriteEmptyDescription() {
		val storeWriter = mock<StoreWriter>(MockMode.autofill)
		val describable = TestDescribable("")
		describable.description.write("desc", storeWriter)

		verify(exactly(0)) { storeWriter.writeStorables(any(), any()) }
	}

	@Test
	fun shouldWriteNonEmptyDescription() {
		val storeWriter = mock<StoreWriter>(MockMode.autofill)
		val slot = Capture.slot<Iterator<Translation>>()
		val describable = TestDescribable("Test")

		every { storeWriter.writeStorables("desc", capture(slot)) } returns Unit
		describable.description.write("desc", storeWriter)

		assertEquals("Test", slot.get().next().text)
	}

	private class TestDescribable(
		description: Description
	) : Describable {

		var changed: Boolean = false
			private set

		constructor(initialValue: String): this(Description(initialValue))

		override var description: Description by observableDescription(description) { changed = true }
	}
}