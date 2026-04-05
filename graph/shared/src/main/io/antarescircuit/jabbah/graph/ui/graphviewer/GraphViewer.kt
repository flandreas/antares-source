package io.antarescircuit.jabbah.graph.ui.graphviewer

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.execution.ExecutionControlOutlet
import io.antarescircuit.jabbah.execution.PauseOrResumeActionImpl
import io.antarescircuit.jabbah.execution.SingleStepModeAction
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.scheduler.SchedulerImpl
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.app.*
import io.antarescircuit.jabbah.graph.app.ApplicationMode.*
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewController
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

interface GraphViewerView : UIView { }

/**
 * Displays a single [GraphView] and allows the user to start execution of this [GraphView].
 * Doesn't feature editing capabilities. Has its own independent [GraphApplicationContextHolder]
 * to start independent simulations of the [GraphView].
 */
class GraphViewerController(
	graphView: GraphView? = null,
	displayGlobalMessages: Boolean = false,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<GraphViewerView>(), ApplicationModeHolder, ExecutionControlOutlet {

	companion object {
		private val LOG by logger(GraphViewerController::class)
	}

	private val systemSpeed = SystemSpeed(eventBus = eventBus)

	private val systemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed)

	/** Spawns an individual [GraphApplicationContextHolder] with its separate [Scheduler] instance.*/
	val applicationContextHolder = GraphApplicationContextHolder(
		SchedulerImpl(systemSpeedCategory),
		systemSpeed = systemSpeed,
		currentSystemSpeedCategory = systemSpeedCategory)

	override var currentMode: ApplicationMode = EDIT
		private set

	val drawingView = EditModule.drawingViewFactory.create(
		(graphView ?: GraphViewModule.graphViewFactory.create(null)) as Drawing<Component>,
		applicationContextHolder,
		displayGlobalMessages,
		""
	) as DrawingView<GraphView>

	val graphNavigationViewController = GraphNavigationViewController(isRoot = true, drawingView)

	init {
		// Cyclic dependency
		applicationContextHolder.applicationModeHolder = this
	}

	override fun dispose() {
		super.dispose()
		LOG.userTrail("Close separate viewer for '${graphNavigationViewController.drawingView.drawing.graph!!.name.value}'")
		applicationContextHolder.scheduler.dispose()
		graphNavigationViewController.dispose()
		toggleApplicationModeAction.dispose()
		singleStepModeAction.dispose()
		pauseOrResumeAction.dispose()
		systemSpeedCategory.dispose()
	}

	/** ---- [ExecutionControlOutlet] */

	override val toggleApplicationModeAction = ToggleApplicationModeAction(null, this, eventBus)

	override val singleStepModeAction = SingleStepModeAction(applicationContextHolder.scheduler, eventBus)

	override val pauseOrResumeAction = PauseOrResumeActionImpl(applicationContextHolder.scheduler, eventBus)

	override val systemSpeedCategoryName: String
		get() = applicationContextHolder.currentSystemSpeedCategory.systemSpeedCategory.toString()

	override var currentSystemSpeed: Int
		get() = applicationContextHolder.currentSystemSpeedCategory.systemSpeed.speed
		set(value) {
			applicationContextHolder.currentSystemSpeedCategory.systemSpeed.speed = value
		}

	/** ---- [ApplicationModeHolder] interface */

	override fun setMode(mode: ApplicationMode, after: () -> Unit) {
		if (mode == currentMode) {
			return
		}
		when (mode) {
			EDIT -> quitExecutionMode()
			EXECUTE, EXEC_USECASE -> enterExecutionMode(mode, after)
		}
	}

	/** ---- [GraphView] */

	fun setMetaGraph(metaGraph: MetaGraph) {
		val clone = metaGraph.cloneGraphGraphStorable()
		LOG.userTrail("Show '${clone.graphView.graph!!.name.value}' in separate viewer")
		graphNavigationViewController.setRootGraphView(clone.graphView, editable = false)
	}

	private fun quitExecutionMode() {
		currentMode = EDIT
		applicationContextHolder.scheduler.isActive = false
		eventBus.post(ApplicationModeEvent(this, currentMode))
	}

	private fun enterExecutionMode(mode: ApplicationMode, after: () -> Unit) {
		eventBus.post(ApplicationModeBeginEvent(this, mode))
		currentMode = mode
		System.invokeLater {
			applicationContextHolder.scheduler.isActive = true
			eventBus.post(ApplicationModeEvent(this, currentMode))
			after()
		}
	}

	override fun updateEditorEditability() { }
}