package ch.scorpion.jabbah.graph.ui.graphviewer

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.PauseOrResumeAction
import ch.scorpion.jabbah.execution.SingleStepModeAction
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.*
import ch.scorpion.jabbah.graph.app.ApplicationMode.*
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewController
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

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
) : AbstractUIController<GraphViewerView>(), ApplicationModeHolder {

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
		displayGlobalMessages) as DrawingView<GraphView>

	val graphNavigationViewController = GraphNavigationViewController(isRoot = true, drawingView)

	val toggleApplicationModeAction = ToggleApplicationModeAction(null, this, eventBus)
	val singleStepModeAction = SingleStepModeAction(applicationContextHolder.scheduler, eventBus)
	val pauseOrResumeAction = PauseOrResumeAction(applicationContextHolder.scheduler, eventBus)

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

	fun setMetaGraph(metaGraph: MetaGraph) {
		val clone = metaGraph.cloneGraphGraphStorable()
		LOG.userTrail("Show '${clone.graphView.graph!!.name.value}' in separate viewer")
		graphNavigationViewController.setRootGraphView(clone.graphView, editable = false)
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