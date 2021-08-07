package ch.scorpion.jabbah.graph.ui.graphviewer

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.app.*
import ch.scorpion.jabbah.graph.app.ApplicationMode.*
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewController
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

interface GraphViewerView : UIView

/**
 * Displays a single [GraphView] and allows the user to start execution of this [GraphView].
 * Doesn't feature editing capabilities.
 */
class GraphViewerController(
	val scheduler: Scheduler = ExecutionModule.scheduler,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<GraphViewerView>(), ApplicationModeHolder {

	companion object {
		private val LOG by logger(GraphViewerController::class)
	}

	override var currentMode: ApplicationMode = EDIT
		private set

	val drawingView = EditModule.drawingViewFactory.invoke(
		GraphViewModule.graphViewFactory.invoke(null) as Drawing<Component>
	) as DrawingView<GraphView>

	val graphNavigationViewController = GraphNavigationViewController(isRoot = false, drawingView)

	val toggleApplicationModeAction = ToggleApplicationModeAction(null, this, eventBus)

	override fun dispose() {
		super.dispose()
		LOG.debug("Close separate viewer for '${graphNavigationViewController.drawingView.drawing.graph!!.name.value}'")
		graphNavigationViewController.dispose()
		toggleApplicationModeAction.dispose()
	}

	fun setGraphView(graphView: GraphView) {
		LOG.debug("Show '${graphView.graph!!.name.value}' in separate viewer")
		graphNavigationViewController.setRootGraphView(graphView, editable = false)
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
		scheduler.isActive = false
		eventBus.post(ApplicationModeEvent(this, currentMode))
	}

	private fun enterExecutionMode(mode: ApplicationMode, after: () -> Unit) {
		eventBus.post(ApplicationModeBeginEvent(this, mode))
		currentMode = mode
		System.invokeLater {
			scheduler.isActive = true
			eventBus.post(ApplicationModeEvent(this, currentMode))
			after()
		}
	}

	override fun updateEditorEditability() { }
}