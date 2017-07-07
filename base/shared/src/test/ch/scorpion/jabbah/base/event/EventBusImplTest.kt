package ch.scorpion.jabbah.base.event

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [EventBusImpl].
 */
class EventBusImplTest {

    val eventBus = EventBusImpl()

    private class TestEventA
    private class TestEventB

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldRegisterAndHandle() {
        var handledA: Boolean = false
        var handledB: Boolean = false
        eventBus.register(TestEventA::class, {handledA = true})
        eventBus.post(TestEventA())
        eventBus.post(TestEventB())
        assertThat(handledA, `is`(true));
        assertThat(handledB, `is`(false));
    }

    @Test
    fun shouldUnregisterForEvent() {
        var handledA: Boolean = false
        val handlerA: (T: Any) -> Unit = {handledA = true}
        eventBus.register(TestEventA::class, handlerA)
        eventBus.unregister(TestEventA::class, handlerA)
        eventBus.post(TestEventA())
        assertThat(handledA, `is`(false));
    }

    @Test
    fun shouldUnregisterAll() {
        var handled: Boolean = false
        val handler: (T: Any) -> Unit = {handled = true}
        eventBus.register(TestEventA::class, handler)
        eventBus.register(TestEventB::class, handler)
        eventBus.unregister(handler)

        eventBus.post(TestEventA())
        assertThat(handled, `is`(false));

        eventBus.post(TestEventB())
        assertThat(handled, `is`(false));
    }
}