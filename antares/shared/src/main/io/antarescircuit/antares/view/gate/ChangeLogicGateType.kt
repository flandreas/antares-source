package io.antarescircuit.antares.view.gate

import io.antarescircuit.antares.model.gate.LogicGateType
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.app.AbstractSelectionAwareAction
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.app.AbstractGraphViewCommand
import io.antarescircuit.jabbah.graph.view.GraphView

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
			view as DrawingView<GraphView>))
	}
}

private class ChangeLogicGateTypeCommand(
	private val newType: LogicGateType,
	private val componentIds: Collection<Int>,
	drawingView: DrawingView<GraphView>
) : AbstractGraphViewCommand("antares.action.changeLogicGateType.name", drawingView), Undoable {

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