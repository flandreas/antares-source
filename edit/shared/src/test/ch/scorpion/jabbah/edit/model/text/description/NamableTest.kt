package ch.scorpion.jabbah.edit.model.text.description

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.module.EditModule
import kotlin.test.*

class NamableTest {

	@BeforeTest
	fun setup() {
		EditModule.require()
	}

	@Test
	fun shouldGetName() {
		val namable = TestNamable("Test")
		assertEquals("Test", namable.name.value)
	}

	@Test
	fun shouldSetName() {
		val namable = TestNamable("Test")
		namable.name = Name("Changed")
		assertEquals("Changed", namable.name.value)
	}

	@Test
	fun shouldPostNameChangedEvent() {
		val namable = TestNamable("Test")
		lateinit var event: NameChangedEvent
		BaseModule.eventBus.register(NameChangedEvent::class) { event = it }

		namable.name = Name("Changed")

		assertNotNull(event)
		assertEquals(namable, event.owner)
		assertEquals("Test", event.oldValue.value)
		assertEquals("Changed", event.name.value)
	}

	private class TestNamable(
		initialValue: String
	) : Namable {
		override var name: Name by observableName(Name(initialValue))
	}
}