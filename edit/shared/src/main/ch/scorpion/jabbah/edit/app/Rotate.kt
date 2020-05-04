package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
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
		commandManager.execute(RotateCommand(drawingView!!, singleSelection!!.id, singleSelection!!.rotation.next()))
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && selectionCount == 1 && singleSelection!!.rotatable
	}
}

/** Rotates a [Component] by a given angle.*/
private class RotateCommand(
	private val drawingView: DrawingView<*>,
	val componentId: Int,
	val rotation: Rotation
) : AbstractCommand("edit.command.rotate", null), Undoable {

	private val component: Component get() = drawingView.drawing.getWithId(componentId) as Component

	override fun execute() {
		component.rotation = rotation
	}

	override fun undo() {
		component.rotation = oldRotation
	}

	override fun validate() {
		component.validate()
	}

	private val oldRotation = component.rotation
}