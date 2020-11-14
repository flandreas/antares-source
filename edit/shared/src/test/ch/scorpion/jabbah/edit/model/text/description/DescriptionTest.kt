package ch.scorpion.jabbah.edit.model.text.description

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.module.EditModule
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

	private class TestDescribable(
		description: Description
	) : Describable {

		var changed: Boolean = false
			private set

		constructor(initialValue: String): this(Description(initialValue))

		override var description: Description by observableDescription(description) { changed = true }
	}
}