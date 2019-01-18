package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * An [Action] for cutting the selected [Component]s from the [Drawing] of the current
 * [View] to the clipboard.
 */
class CutAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	private val typeMap: TypeMap = IOModule.typeMap,
	private val cmdManager: CommandManager = EditModule.commandManager,
	private val scheduler: Scheduler = ExecutionModule.scheduler
) : AbstractSelectionAwareAction("edit.action.cut", eventBus, viewManager) {

	init {
		eventBus.register(SchedulerActivationStateEvent::class) { updateEnabled() }
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val drawingView = viewManager.activeView as DrawingView<Drawing<Component>>
		CopyPasteUtilitySwing.cut(
			drawingView,
			drawingView.selectionManager.selection,
			typeMap,
			cmdManager)
	}

	override fun calculateEnabled(): Boolean = super.calculateEnabled() && !scheduler.isActive
}