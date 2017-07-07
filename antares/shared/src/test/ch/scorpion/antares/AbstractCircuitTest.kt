package ch.scorpion.antares

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import org.junit.Before
import org.junit.ClassRule

/**
 * A test base class for testing Antares circuit simulations.
 */
abstract class AbstractCircuitTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    protected lateinit var styleProvider: StyleProvider
    protected lateinit var eventBus: EventBus
    protected lateinit var timeService: ControlledTimeService
    protected lateinit var timer: Timer
    protected lateinit var scheduler: Scheduler

    @Before
    fun setup() {
        TestTranslationsBuilder().withAnyKey()

        styleProvider = DrawStyleModule.styleProvider
        eventBus = EventBusImpl()
        timeService = ControlledTimeService()
        timer = ControlledTimer(timeService)
        scheduler = SchedulerImpl(timeService, timer, eventBus, NoiseGeneratorHolder())
    }

    abstract fun getCircuitView(): GraphView<GraphElementView<*>>

    protected fun startSimulation() {
        scheduler.isActive = true
        getCircuitView().graph!!.executionStarted(scheduler)
    }

    protected fun stopSimulation() {
        scheduler.isActive = false
        getCircuitView().graph!!.executionStopped(scheduler)
    }

    protected fun proceedToMillis(timeMillis: Long) {
        timeService.setTimeMillis(timeMillis)
        scheduler.proceedTo(timeMillis * 1_000_000)
    }

    protected fun proceedToNanos(timeNanos: Long) {
        timeService.setTimeNanos(timeNanos)
        scheduler.proceedTo(timeNanos)
    }
}