package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * An [Action] for copying the selected [Component]s to the clipboard.
 */
class CopyAction(
    eventBus: EventBus = BaseModule.eventBus,
    viewManager: ViewManager = DrawViewModule.viewManager,
    val typeMap: TypeMap = IOModule.typeMap,
    private val scheduler: Scheduler = ExecutionModule.scheduler
) : AbstractSelectionAwareAction("edit.action.copy", eventBus, viewManager) {

	init {
		eventBus.register(SchedulerActivationStateEvent::class) { updateEnabled() }
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        val drawingView = viewManager.activeView as DrawingView<*>
        CopyPasteUtilitySwing.copy(
                drawingView.drawing as GraphView<*>,
                drawingView.selectionManager.selection,
                typeMap)
    }

	override fun calculateEnabled(): Boolean = super.calculateEnabled() && !scheduler.isActive
}