package ch.scorpion.jabbah.base.event

import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.*

/** Unit tests for [EventBusImpl]. */
class EventBusImplTest {

	val eventBus = EventBusImpl()

	private class TestEventA
	private class TestEventB

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

	@Test
	fun shouldRegisterAndHandle() {
		var handledA = false
		val handledB = false
		eventBus.register(TestEventA::class) { handledA = true }
		eventBus.post(TestEventA())
		eventBus.post(TestEventB())
		assertTrue(handledA)
		assertFalse(handledB)
	}

	@Test
	fun shouldUnregisterForEvent() {
		var handledA = false
		val handlerA: (T: Any) -> Unit = { handledA = true }
		eventBus.register(TestEventA::class, handlerA)
		eventBus.unregister(TestEventA::class, handlerA)
		eventBus.post(TestEventA())
		assertFalse(handledA)
	}

	@Test
	fun shouldUnregisterAll() {
		var handled = false
		val handler: (T: Any) -> Unit = { handled = true }
		eventBus.register(TestEventA::class, handler)
		eventBus.register(TestEventB::class, handler)
		eventBus.unregister(handler)

		eventBus.post(TestEventA())
		assertFalse(handled)

		eventBus.post(TestEventB())
		assertFalse(handled)
	}
}