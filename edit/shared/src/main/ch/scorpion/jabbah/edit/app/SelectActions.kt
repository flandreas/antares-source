package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.command.AbstractCommandManagerAwareAction
import ch.scorpion.jabbah.edit.module.EditModule

/** An [Action] for selecting all [Component]s in a [Drawing].*/
class SelectAllAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	commandManager: CommandManager = EditModule.commandManager
) : AbstractCommandManagerAwareAction("edit.action.selectAll", commandManager, eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		(viewManager.activeView as DrawingView<*>).selectionManager.selectAll()
		viewManager.activeView!!.repaint()
	}
}

/** An [Action] for selecting the next [Component] in a [Drawing].*/
class SelectNextAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	commandManager: CommandManager = EditModule.commandManager
) : AbstractCommandManagerAwareAction("edit.action.selectNext", commandManager, eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		(viewManager.activeView as DrawingView<*>).selectionManager.selectNext()
		viewManager.activeView!!.repaint()
	}
}

/** An [Action] for selecting the previous [Component] in a [Drawing].*/
class SelectPreviousAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	commandManager: CommandManager = EditModule.commandManager
) : AbstractCommandManagerAwareAction("edit.action.selectPrevious", commandManager, eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		(viewManager.activeView as DrawingView<*>).selectionManager.selectPrevious()
		viewManager.activeView!!.repaint()
	}
}