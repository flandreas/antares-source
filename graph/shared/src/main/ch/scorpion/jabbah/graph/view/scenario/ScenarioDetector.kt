package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.scheduler.*
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioEvent
import ch.scorpion.jabbah.graph.view.ScenarioStep
import ch.scorpion.jabbah.graph.view.ScenarioStepEvent
import ch.scorpion.jabbah.base.loggerFor

/**
 *  Detects the start of a [Scenario] in an executing [Graph] and notifies this by posting a
 * [ScenarioEvent] on the [EventBus].
 *
 * A [ScenarioDetector] is only active if the [Scheduler]'s [SchedulerRunningState] is
 * [SchedulerRunningState.PAUSED], that is if the executing is stepping.
 */
class ScenarioDetector(
        private val view: DrawingView<GraphView<GraphElementView<*>>>,
        private val scheduler: Scheduler,
        private val scriptGateway: ScriptGateway,
        private val eventBus: EventBus
) {

    private val LOG by loggerFor(this)

    private val schedulerEventHandler: EventHandler<SchedulerEvent> = {
        if (it.actor is GraphElement && view.drawing.graph!!.contains(it.actor as GraphElement)) {
            detect()
        }
    }
    private val schedulerActivateStateHandler: EventHandler<SchedulerActivationStateEvent> = { updateActive() }
    private val schedulerRunningStateHandler: EventHandler<SchedulerRunningStateEvent> = { updateActive() }
    private val scenarioStepEventHandler: EventHandler<ScenarioStepEvent> = {
        it.oldStep?.passivate(view)
        it.newStep?.activate(view)
    }

    private var isActive: Boolean = false

    init {
        eventBus.register(SchedulerEvent::class, schedulerEventHandler)
        eventBus.register(SchedulerActivationStateEvent::class, schedulerActivateStateHandler)
        eventBus.register(SchedulerRunningStateEvent::class, schedulerRunningStateHandler)
        eventBus.register(ScenarioStepEvent::class, scenarioStepEventHandler)

        updateActive()
    }

    fun dispose() {
        eventBus.unregister(SchedulerEvent::class, schedulerEventHandler)
        eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivateStateHandler)
        eventBus.unregister(SchedulerRunningStateEvent::class, schedulerRunningStateHandler)
        eventBus.unregister(ScenarioStepEvent::class, scenarioStepEventHandler)
    }

    private fun detect() {
        if (isActive) {
            view.drawing.currentScenario = view.drawing.scenarios.getScenarios().firstOrNull {
                it.condition.invoke(view, scriptGateway)
            }

            val scenario = view.drawing.currentScenario
            if (scenario != null) {
                setCurrentScenarioStep(scenario.getScenarioSteps().firstOrNull {
                    it.condition.invoke(view, scriptGateway)
                })
            } else {
                setCurrentScenarioStep(null)
            }
        }
    }

    private fun updateActive() {
        val oldValue = isActive
        isActive = scheduler.isActive && scheduler.isPaused
        if (isActive != oldValue) {
            LOG.debug("ScenarioDetector: active = '$isActive'")
            view.drawing.currentScenario = null
            view.drawing.currentScenarioStep = null
        }
    }

    private fun setCurrentScenarioStep(scenarioStep: ScenarioStep?) {
        if (scenarioStep == null) {
            LOG.debug("ScenarioDetector: no current ScenarioStep")
        } else {
            LOG.debug("ScenarioDetector: detected ScenarioStep '${scenarioStep.name}'")
        }
        view.drawing.currentScenarioStep = scenarioStep
    }
}