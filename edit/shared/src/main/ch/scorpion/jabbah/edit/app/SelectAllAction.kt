package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.module.EditModule

/** An [Action] for selecting all [Component]s in a [Drawing].*/
class SelectAllAction(
        eventBus: EventBus = BaseModule.eventBus,
        viewManager: ViewManager = DrawViewModule.viewManager,
        private val commandManager: CommandManager = EditModule.commandManager
) : AbstractViewAction("edit.action.selectAll", eventBus, viewManager) {

	init {
		eventBus.register(CommandManagerActiveEvent::class) { updateEnabled() }
	}

    override fun execute(event: ActionEvent) {
        (viewManager.activeView as DrawingView<*>).selectionManager.selectAll()
        viewManager.activeView!!.repaint()
    }

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && commandManager.active
	}
}