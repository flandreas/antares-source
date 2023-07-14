package ch.scorpion.antares.view.gate

import ch.scorpion.jabbah.base.Action
import ch.scorpion.antares.model.gate.LogicGateType
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * An [Action] for changing the [LogicGateType] of the selected [LogicGateView]s.
 */
class ChangeLogicGateTypeAction(
	private val newType: LogicGateType,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	private val commandManager: CommandManager = EditModule.commandManager
) : AbstractSelectionAwareAction(newType.baseResourceKey, eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		commandManager.execute(ChangeLogicGateTypeCommand(
			newType,
			selection.map { it.id },
			view as DrawingView<*>))
	}
}

private class ChangeLogicGateTypeCommand(
	private val newType: LogicGateType,
	private val componentIds: Collection<Int>,
	private val drawingView: DrawingView<*>
) : AbstractCommand("antares.action.changeLogicGateType.name"), Undoable {

	private val components: Collection<Component> get() = componentIds.map { drawingView.drawing.getWithId(it)!! }.toList()

	private val oldTypes: Map<Int, LogicGateType> = components.associateBy(
		{ it.id },
		{ (it as LogicGateView).model.gateType})


	override fun execute() {
		components
			.map { it as LogicGateView }
			.forEach { it.logicGateType = newType }
	}

	override fun undo() {
		components
			.map { it as LogicGateView }
			.forEach { it.logicGateType = oldTypes[it.id]!! }
	}
}