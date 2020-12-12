package ch.scorpion.jabbah.edit.model.text.description

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.io.StoreWriter
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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
		val storeWriter = mockk<StoreWriter>(relaxed = true)
		val describable = TestDescribable("")
		describable.description.write("desc", storeWriter)

		verify(exactly = 0) { storeWriter.writeStorables(any(), any()) }
	}

	@Test
	fun shouldWriteNonEmptyDescription() {
		val storeWriter = mockk<StoreWriter>(relaxed = true)
		val slot = slot<Iterator<Translation>>()
		val describable = TestDescribable("Test")

		describable.description.write("desc", storeWriter)

		verify(exactly = 1) { storeWriter.writeStorables("desc", capture(slot)) }
		assertEquals("Test", slot.captured.next().text)
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