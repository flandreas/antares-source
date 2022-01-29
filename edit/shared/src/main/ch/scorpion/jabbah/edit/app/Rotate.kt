package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule

class RotateAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	private val commandManager: CommandManager = EditModule.commandManager
) : AbstractSelectionAwareAction("edit.action.rotate", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		commandManager.execute(RotateCounterclockwiseCommand(drawingView!!, singleSelection!!.id))
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && selectionCount == 1 && singleSelection!!.rotatable
	}
}

/** Rotates a [Component] to the given [Rotation].*/
private class RotateCounterclockwiseCommand(
	private val drawingView: DrawingView<*>,
	val componentId: Int
) : AbstractCommand("edit.command.rotate", null), Undoable {

	private val component: Component get() = drawingView.drawing.getWithId(componentId) as Component

	override fun execute() {
		component.rotate(RotationDirection.CounterClockwise)
	}

	override fun undo() {
		component.rotate(RotationDirection.Clockwise)
	}

	override fun validate() {
		component.validate()
	}
}