package io.antarescircuit.jabbah.base.event

import io.antarescircuit.jabbah.base.module.BaseModule
import kotlin.test.*

class EventBusImplTest {

	private val eventBus = EventBusImpl()

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

	@Test
	fun shouldCountRegisteredHandlers() {
		val handler: (T: Any) -> Unit = {}
		eventBus.register(TestEventA::class, handler)
		eventBus.register(TestEventB::class, handler)
		assertEquals(2, eventBus.size)

		eventBus.unregister(handler)
		assertEquals(0, eventBus.size)
	}

	@Test
	fun shouldAllowHandlersToUnregisterDuringTwoPhasePost() {
		var calls = 0
		lateinit var selfRemoving: (Any) -> Unit
		selfRemoving = { calls++; eventBus.unregister(TestEventA::class, selfRemoving) }
		eventBus.register(TestEventA::class, selfRemoving)
		eventBus.register(TestEventA::class) { calls++ }

		eventBus.postTwoPhase(TestEventA(), thenHandler = {})
		eventBus.postTwoPhase(TestEventA(), TestEventB())

		assertEquals(3, calls)
	}
}
