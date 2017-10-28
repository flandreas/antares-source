package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
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
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.drawable.FlexibleTextView
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategoryEvent
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 *  Detects the start of a [Scenario] or a [ScenarioStep] in an executing [Graph] and propagates this
 *  by setting the corresponding properties of the associated [GraphView], which in turn posts
 *  a [ScenarioEvent] or a [ScenarioStepEvent] on its [EventBus].
 *
 * A [ScenarioDetector] is only active if the [Scheduler]'s [SchedulerRunningState] is
 * [SchedulerRunningState.PAUSED], that is if the executing is stepping.
 */
class ScenarioDetector(
        private val view: DrawingView<GraphView<GraphElementView<*>>>,
        private val scheduler: Scheduler,
        private val scriptGateway: ScriptGateway,
        private val eventBus: EventBus,
        private val currentSystemSpeedCategory: CurrentSystemSpeedCategory
) {

    companion object {
        private val LOG by logger(ScenarioDetector::class)

        private val SCENARIO_STEP_DESC_WIDTH = 400

        private val DESC_DISTANCE = 20

        private val DESC_UNZOOMABLE = false
    }

    private val schedulerEventHandler: EventHandler<SchedulerEvent> = {
        if (it.actor is GraphElement && view.drawing.graph!!.contains(it.actor as GraphElement)) {
            detect()
        }
    }

    private val systemSpeedCategoryHandler: EventHandler<SystemSpeedCategoryEvent> = { updateActive() }

    private val schedulerActivateStateHandler: EventHandler<SchedulerActivationStateEvent> = { updateActive() }

    private val scenarioEventHandler: EventHandler<ScenarioEvent> = {
        hideScenarioDesc()
        it.scenario?.let { displayScenarioDesc(it) }
        view.repaint()
    }

    private val scenarioStepEventHandler: EventHandler<ScenarioStepEvent> = {
        it.oldStep?.passivate(view)
        unhighlightScenarioStep()
        hideScenarioStepDesc()
        it.newStep?.let {
            displayScenarioStepDesc(it)
            highlightScenarioStep(it)
        }
        it.newStep?.activate(view)
        view.repaint()
    }

    private var isActive: Boolean = false

    /** The currently displayed description of a [Scenario], if any.*/
    private var scenarioDesc: FlexibleTextView? = null

    /** The currently displayed description of a [ScenarioStep], if any.*/
    private var scenarioStepDesc: FlexibleTextView? = null

    /** The IDs of the currently highlighted [Component]s, if any.*/
    private var highlightIds: List<Int>? = null

    init {
        eventBus.register(SystemSpeedCategoryEvent::class, systemSpeedCategoryHandler)
        eventBus.register(SchedulerEvent::class, schedulerEventHandler)
        eventBus.register(SchedulerActivationStateEvent::class, schedulerActivateStateHandler)
        eventBus.register(ScenarioEvent::class, scenarioEventHandler)
        eventBus.register(ScenarioStepEvent::class, scenarioStepEventHandler)

        updateActive()
    }

    fun dispose() {
        eventBus.unregister(SystemSpeedCategoryEvent::class, systemSpeedCategoryHandler)
        eventBus.unregister(SchedulerEvent::class, schedulerEventHandler)
        eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivateStateHandler)
        eventBus.unregister(ScenarioStepEvent::class, scenarioStepEventHandler)
        eventBus.unregister(ScenarioEvent::class, scenarioEventHandler)
    }

    private fun detect() {
        if (isActive) {
            view.drawing.currentScenario = view.drawing.scenarios.getScenarios().firstOrNull {
                it.condition.invoke(view, scriptGateway)
            }
        }

        // An occurred Issue could have deactivated the Scheduler
        if (isActive) {
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
        isActive = scheduler.isActive && currentSystemSpeedCategory.systemSpeedCategory >= SystemSpeedCategory.Explore
        if (isActive != oldValue) {
            LOG.debug("ScenarioDetector: active = '$isActive'")
            view.drawing.currentScenario = null
            view.drawing.currentScenarioStep = null
        }
    }

    private fun setCurrentScenarioStep(scenarioStep: ScenarioStep?) {
        if (isActive) {
            if (scenarioStep == null) {
                LOG.debug("ScenarioDetector: no current ScenarioStep")
            } else {
                LOG.debug("ScenarioDetector: detected ScenarioStep '${scenarioStep.name}'")
            }
            view.drawing.currentScenarioStep = scenarioStep
        }
    }

    private fun displayScenarioDesc(scenario: Scenario) {
        if (StringUtils.isNotEmpty(scenario.description.text)) {
            scenarioDesc = FlexibleTextView(
                    scenario.description.text!!,
                    calculateScenarioDescAnchor(),
                    Direction.NORTH,
                    SCENARIO_STEP_DESC_WIDTH,
                    DESC_UNZOOMABLE,
                    GraphStyleType.EXPLANATION)
            if (DESC_UNZOOMABLE) view.ghostContainer.add(scenarioDesc!!) else view.animationContainer.add(scenarioDesc!!)
            scenarioDesc!!.validate()
        }
    }

    private fun displayScenarioStepDesc(scenarioStep: ScenarioStep) {
        if (StringUtils.isNotEmpty(scenarioStep.description.text)) {
            scenarioStepDesc = FlexibleTextView(
                    scenarioStep.description.text!!,
                    calculateScenarioStepDescAnchor(),
                    Direction.SOUTH,
                    SCENARIO_STEP_DESC_WIDTH,
                    DESC_UNZOOMABLE,
                    GraphStyleType.EXPLANATION)
            if (DESC_UNZOOMABLE) view.ghostContainer.add(scenarioStepDesc!!) else view.animationContainer.add(scenarioStepDesc!!)
            scenarioStepDesc!!.validate()
        }
    }

    private fun hideScenarioDesc() {
        if (scenarioDesc != null) {
            if (DESC_UNZOOMABLE) {
                view.ghostContainer.remove(scenarioDesc!!)
                view.ghostContainer.validate()
            } else {
                view.animationContainer.remove(scenarioDesc!!)
                view.animationContainer.validate()
            }
            scenarioDesc = null
        }
    }

    private fun hideScenarioStepDesc() {
        if (scenarioStepDesc != null) {
            if (DESC_UNZOOMABLE) {
                view.ghostContainer.remove(scenarioStepDesc!!)
                view.ghostContainer.validate()
            } else {
                view.animationContainer.remove(scenarioStepDesc!!)
                view.animationContainer.validate()
            }
            scenarioStepDesc = null
        }
    }

    /** Highlight the [Component]s as required by the specified [ScenarioStep].*/
    private fun highlightScenarioStep(scenarioStep: ScenarioStep) {
        highlightIds = scenarioStep.highlightIdsAsInt
        if (!highlightIds!!.isEmpty()) {
            view.highlighter.highlight(*highlightIds!!.toIntArray())
        }
    }

    /** Removes the highlights that have been added by the current [ScenarioStep].*/
    private fun unhighlightScenarioStep() {
        highlightIds?.let {
            if (!it.isEmpty()) {
                view.highlighter.unhighlight(*it.toIntArray())
            }
        }
    }

    /** Unhighlight the [Component]s that have been highlighted by the specified [ScenarioStep].*/

    private fun calculateScenarioDescAnchor(): Point2D {
        val bounds = view.contentBounds
        return Point2D(bounds.centerX, bounds.minY - DESC_DISTANCE)
    }

    private fun calculateScenarioStepDescAnchor(): Point2D {
        val bounds = view.contentBounds
        return Point2D(bounds.centerX, bounds.maxY + DESC_DISTANCE)
    }
}