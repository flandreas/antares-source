package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.drawable.FlexibleTextView
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.scheduler.BreakEvent
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.execution.scheduler.SchedulerEvent
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategoryEvent
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 *  Detects the start of a [Scenario] or a [ScenarioStep] in an executing [Graph] and propagates this
 *  by setting the corresponding properties of the associated [GraphView], which in turn posts
 *  a [ScenarioEvent] or a [ScenarioStepEvent] on its [EventBus].
 */
class ScenarioDetector(
	private val view: DrawingView<GraphView>,
	private val applicationContextHolder: GraphApplicationContextHolder,
	private val eventBus: EventBus
) {

	companion object {

		/** The custom name [String] of the limit [SystemSpeedCategory] in [Properties]. */
		const val PROP_LIMIT_SYSTEM_SPEED_CATEGORY = "graph.view.scenario.detector.systemSpeedCategoryLimit"

		private val LOG by logger(ScenarioDetector::class)

		private const val SCENARIO_STEP_DESC_WIDTH = 400

		private const val DESC_DISTANCE = 20

		private const val DESC_UNZOOMABLE = false
	}

	private val schedulerEventHandler: EventHandler<SchedulerEvent> = {
		if (isAfterStartupDuration()) {
			val doDetect =
				if (it.scheduler === applicationContextHolder.scheduler && it.type == SchedulerEvent.Type.DONE) {
					when (it.source) {
						is GraphElement -> {
							view.drawing.graph!!.contains(it.source as GraphElement)
						}

						is GraphView -> {
							view.drawing === it.source
						}

						else -> false
					}
				} else {
					false
				}

			if (doDetect) {
				detect()
			}
		}
	}

	private fun isAfterStartupDuration(): Boolean =
		view.drawing.graph?.startupTime?.let {
			applicationContextHolder.scheduler.executionTime >= it
		} ?: true

	private val systemSpeedCategoryHandler: EventHandler<SystemSpeedCategoryEvent> = {
		if (it.source === applicationContextHolder.currentSystemSpeedCategory) {
			updateActive()
		}
	}

	private val schedulerActivateStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (it.scheduler === applicationContextHolder.scheduler) {
			updateActive()
		}
	}

	private val scenarioEventHandler: EventHandler<ScenarioEvent> = { event ->
		if (event.graphView === view.drawing) {
			hideScenarioDesc()
			event.scenario?.let { displayScenarioDesc(it) }
			view.repaint()
		}
	}

	private val scenarioStepEventHandler: EventHandler<ScenarioStepEvent> = {
		if (it.graphView === view.drawing) {
			it.oldStep?.passivate(view)
			unhighlightScenarioStep()
			hideScenarioStepDesc()

			it.newStep?.let { newStep ->
				displayScenarioStepDesc(newStep)
				highlightScenarioStep(newStep)
				newStep.activate(view)
				if (applicationContextHolder.scenarioBreakpoints.enabled) {
					LOG.trace("Breaking at scenario step ${newStep.name.getTranslation()}")
					eventBus.post(BreakEvent())
				}
			}

			view.repaint()
		}
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
			val detectedScenario = view.drawing.scenarios.getScenarios().firstOrNull {
				it.condition.invoke(view)
			}
			setCurrentScenario(detectedScenario)
		}

		// An occurred Issue could have deactivated the Scheduler
		if (isActive) {
			val scenario = view.drawing.currentScenario
			if (scenario != null) {
				setCurrentScenarioStep(scenario.getScenarioSteps().firstOrNull {
					it.condition.invoke(view)
				})
			} else {
				setCurrentScenarioStep(null)
			}
		}
	}

	private fun updateActive() {
		val oldValue = isActive
		isActive = applicationContextHolder.scheduler.isActive
			&& applicationContextHolder.currentSystemSpeedCategory.systemSpeedCategory >= SystemSpeedCategory.withName(BaseModule.properties.getString(PROP_LIMIT_SYSTEM_SPEED_CATEGORY))
		if (isActive != oldValue) {
			LOG.trace("active = '$isActive'")
			view.drawing.currentScenario = null
			view.drawing.currentScenarioStep = null
		}
	}

	private fun setCurrentScenario(scenario: Scenario?) {
		view.drawing.currentScenario = scenario
	}

	private fun setCurrentScenarioStep(scenarioStep: ScenarioStep?) {
		if (isActive && scenarioStep !== view.drawing.currentScenarioStep) {
			if (scenarioStep == null) {
				LOG.trace("No current ScenarioStep")
			} else {
				LOG.trace("${applicationContextHolder.scheduler.executionTime} ns: Detected ScenarioStep '${scenarioStep.name}'")
			}
			view.drawing.currentScenarioStep = scenarioStep
		}
	}

	private fun displayScenarioDesc(scenario: Scenario) {
		if (StringUtils.isNotEmpty(scenario.description.value)) {
			scenarioDesc = FlexibleTextView(
				scenario.description.value!!,
				calculateScenarioDescAnchor(),
				Direction.NORTH,
				SCENARIO_STEP_DESC_WIDTH,
				DESC_UNZOOMABLE,
				view.canvas.devicePixelRatio,
				GraphStyleType.EXPLANATION)
			displayDesc(scenarioDesc!!)
		}
	}

	private fun displayScenarioStepDesc(scenarioStep: ScenarioStep) {
		if (StringUtils.isNotEmpty(scenarioStep.description.value)) {
			scenarioStepDesc = FlexibleTextView(
				scenarioStep.description.value!!,
				calculateScenarioStepDescAnchor(),
				Direction.SOUTH,
				SCENARIO_STEP_DESC_WIDTH,
				DESC_UNZOOMABLE,
				view.canvas.devicePixelRatio,
				GraphStyleType.EXPLANATION)
			displayDesc(scenarioStepDesc!!)
		}
	}

	private fun displayDesc(desc: FlexibleTextView) {
		if (DESC_UNZOOMABLE) {
			view.ghostContainer.add(desc)
		} else {
			view.animationContainer.add(desc)
		}
		view.contentBounds.expandBy(desc)
		desc.validate()
	}

	private fun hideDesc(desc: FlexibleTextView) {
		if (DESC_UNZOOMABLE) {
			view.ghostContainer.remove(desc)
			view.ghostContainer.validate()
		} else {
			view.animationContainer.remove(desc)
			view.animationContainer.validate()
		}
		view.contentBounds.removeExpansion(desc)
	}

	private fun hideScenarioDesc() {
		if (scenarioDesc != null) {
			hideDesc(scenarioDesc!!)
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
		if (highlightIds!!.isNotEmpty()) {
			view.highlighter.highlight(*highlightIds!!.toIntArray())
		}
	}

	/** Removes the highlights that have been added by the current [ScenarioStep].*/
	private fun unhighlightScenarioStep() {
		highlightIds?.let {
			if (it.isNotEmpty()) {
				view.highlighter.unhighlight(*it.toIntArray())
			}
		}
	}

	/** Unhighlight the [Component]s that have been highlighted by the specified [ScenarioStep].*/

	private fun calculateScenarioDescAnchor(): Point2D {
		val bounds = view.contentBounds
		return Point2D(bounds.main.centerX, bounds.main.minY - DESC_DISTANCE)
	}

	private fun calculateScenarioStepDescAnchor(): Point2D {
		val bounds = view.contentBounds
		return Point2D(bounds.main.centerX, bounds.main.maxY + DESC_DISTANCE)
	}
}