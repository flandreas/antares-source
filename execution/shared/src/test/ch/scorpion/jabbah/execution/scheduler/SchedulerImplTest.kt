package ch.scorpion.jabbah.execution.scheduler

import com.nhaarman.mockitokotlin2.*
import ch.scorpion.jabbah.execution.ExecutionTestRule
import org.junit.Assert.*
import org.junit.ClassRule
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.*
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.noise.NoNoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test
import javax.naming.ldap.Control

/**
 * Unit tests for [SchedulerImpl].
 */
class SchedulerImplTest {

    companion object {
        @ClassRule @JvmField
        val testRule = ExecutionTestRule()

        val MILLION = 1000000L
    }

    private val timeService = ControlledTimeService()
    private val eventBus = EventBusImpl()
    private val scheduler: SchedulerImpl;

    init {
        scheduler = SchedulerImpl(
            timeService,
            ControlledTimer(timeService),
            eventBus,
            NoiseGeneratorHolder(NoNoiseGenerator(), eventBus)
        )
    }

    /** ---- Basics tests */

    @Test
    fun shouldActAfterDelay() {
        val actor = createActor()
        scheduler.isActive = true
        scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

        timeService.setTimeMillis(150)

        verify(actor).act(any(), any())
    }

    @Test
    fun shouldNotActBeforeDelay() {
        val actor = createActor()
        scheduler.isActive = true
        scheduler.signalHandler.requestActingAfter(actor, 200 * MILLION, createActorData())

        timeService.setTimeMillis(150)

        verify(actor, never()).act(any(), any())
    }

    @Test
    fun shouldActAllWithSameDelay() {
        val actor1 = createActor()
        val actor2 = createActor()
        scheduler.isActive = true
        scheduler.signalHandler.requestActingAfter(actor1, 100 * MILLION, createActorData())
        scheduler.signalHandler.requestActingAfter(actor2, 100 * MILLION, createActorData())

        timeService.setTimeMillis(150)

        verify(actor1, times(1)).act(any(), any())
        verify(actor2, times(1)).act(any(), any())
    }

    @Test
    fun shouldForgetAfterActing() {
        val actor = createActor()
        scheduler.isActive = true
        scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

        timeService.setTimeMillis(150)
        timeService.setTimeMillis(300)

        verify(actor, times(1)).act(any(), any())
        assertThat(scheduler.numberOfRemainingSlots, `is`(0))
    }

    @Test
    fun shouldActOnlyOnceWhenRequestedTwice() {
        val actor = createActor()
        scheduler.isActive = true
        scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())
        scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

        timeService.setTimeMillis(150)

        verify(actor, times(1)).act(any(), any())
    }

    /** ---- Stepping tests */

    @Test(expected = IllegalStateException::class)
    fun shouldNotAllowStepWhenNotActive() {
        scheduler.isPaused = true
        scheduler.isActive = false
        scheduler.step()
    }

    @Test(expected = IllegalStateException::class)
    fun shouldNotAllowStepWhenNotPaused() {
        scheduler.isPaused = false
        scheduler.isActive = true
        scheduler.step()
    }

    @Test
    fun shouldPause() {
        val actor = createActor()
        scheduler.isActive = true
        scheduler.isPaused = true
        scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

        timeService.setTimeMillis(150)

        verify(actor, never()).act(any(), any())
    }

    @Test
    fun shouldStep() {
        val actor1 = createActor()
        val actor2 = createActor()
        scheduler.isActive = true
        scheduler.isPaused = true
        scheduler.signalHandler.requestActingAfter(actor1, 100 * MILLION, createActorData())
        scheduler.signalHandler.requestActingAfter(actor2, 200 * MILLION, createActorData())

        timeService.setTimeMillis(150)
        scheduler.step()
        verify(actor1, times(1)).act(any(), any())
        verify(actor2, never()).act(any(), any())

        timeService.setTimeMillis(250)
        scheduler.step()
        verify(actor1, times(1)).act(any(), any())
        verify(actor2, times(1)).act(any(), any())
    }

    @Test
    fun shouldNotWaitForRealTimeWhenStepping() {
        val actor1 = createActor()
        val actor2 = createActor()
        scheduler.isActive = true
        scheduler.isPaused = true
        scheduler.signalHandler.requestActingAfter(actor1, 100 * MILLION, createActorData())
        scheduler.signalHandler.requestActingAfter(actor2, 200 * MILLION, createActorData())

        timeService.setTimeMillis(50)
        scheduler.step()
        verify(actor1, times(1)).act(any(), any())
        verify(actor2, never()).act(any(), any())

        timeService.setTimeMillis(80)
        scheduler.step()
        verify(actor1, times(1)).act(any(), any())
        verify(actor2, times(1)).act(any(), any())
    }

    /** ---- Animation tests */

    @Test
    fun shouldWaitForActingDone() {
        val actor1: Actor = mock()
        whenever(actor1.act(any(), any())).thenReturn(false)
        val actor2 = createActor()

        scheduler.isActive = true
        scheduler.signalHandler.requestActingAfter(actor1, 100 * MILLION, createActorData())

        timeService.setTimeMillis(150)
        verify(actor1, times(1)).act(any(), any())
        verify(actor2, never()).act(any(), any())

        timeService.setTimeMillis(300)
        verify(actor1, times(1)).act(any(), any())
        verify(actor2, never()).act(any(), any())
    }

    @Test
    fun shouldProcessSimulationDone() {
        val actor1 = createActor()
        val actor2 = ForwardingActor(actor1)

        scheduler.isActive = true
        scheduler.signalHandler.requestActingAfter(actor2, 100 * MILLION, createActorData())

        timeService.setTimeMillis(150)
        assertThat(actor2.actingCalled, `is`(true))
        verify(actor1, never()).act(any(), any())
        assertThat(scheduler.numberOfRemainingSlots, `is`(1))

        scheduler.signalHandler.actingDone(actor2)
        assertThat(scheduler.numberOfRemainingSlots, `is`(1))

        timeService.setTimeMillis(250)
        verify(actor1, times(1)).act(any(), any())
        assertThat(scheduler.numberOfRemainingSlots, `is`(0))
    }

    @Test
    fun shouldAnimateInParallel() {
        val actor1 = createActor()
        val actor2 = ForwardingActor(actor1)
        val actor3 = ForwardingActor(actor1)

        scheduler.isActive = true
        scheduler.signalHandler.requestActingAfter(actor2, 100 * MILLION, createActorData())
        scheduler.signalHandler.requestActingAfter(actor3, 100 * MILLION, createActorData())
        assertThat(scheduler.numberOfRemainingSlots, `is`(1))

        timeService.setTimeMillis(150)
        verify(actor1, never()).act(any(), any())
        assertThat(actor2.actingCalled, `is`(true))
        assertThat(actor3.actingCalled, `is`(true))

        scheduler.signalHandler.actingDone(actor2)
        scheduler.signalHandler.actingDone(actor3)
        assertThat(scheduler.numberOfRemainingSlots, `is`(1))

        timeService.setTimeMillis(300)
        verify(actor1, times(1)).act(any(), any())
    }

    /** ---- [SchedulerImplTest] */

    private fun createActor(isBreakpoint: Boolean = true): Actor {
        val actor: Actor = mock()
        whenever(actor.act(any(), any())).thenReturn(true)
        whenever(actor.isBreakpoint).thenReturn(isBreakpoint)
        return actor
    }

    private fun createActorData(): ActorData = SimpleActorData()

    /**
     * An [Actor] that does an animation and requires to be called with
     * [Actor.actingDone], in which it requests scheduling of a target [Actor]
     */
    private inner class ForwardingActor(
        private val target: Actor,
        private val targetPropDelay: Long = 100
    ) : Actor {

        var actingCalled: Boolean = false

        /** ---- [Actor] interface */

        override val id: Int get() = 0

        override var state: ActorState = ActorState.NonExecuting
            private set

        override var propagationDelay: Long = 100L

        override val isBreakpoint: Boolean
            get() = true

        override fun addActorListener(l: ActorListener) {
            // empty
        }

        override fun removeActorListener(l: ActorListener) {
            // empty
        }

        override fun executionStarted(signalHandler: SignalHandler) {
            state = ActorState.Idle
        }

        override fun act(signalHandler: SignalHandler, data: ActorData): Boolean {
            state = ActorState.Acting
            actingCalled = true
            return false
        }

        override fun actingVisualized(signalHandler: SignalHandler, l: ActorListener) {
            // empty
        }

        override fun actingDone(signalHandler: SignalHandler, data: ActorData) {
            state = ActorState.Idle
            signalHandler.requestActingAfter(target, targetPropDelay, createActorData())
        }

        override fun executionStopped(signalHandler: SignalHandler) {
           state = ActorState.NonExecuting
        }
    }
}