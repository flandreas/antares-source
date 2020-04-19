package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.edit.CommandEvent
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * An [Action] for saving the current [Storable] with the current file path.
 */
class SaveFileAction(
	application: Application,
	eventBus: EventBus = BaseModule.eventBus,
	val commandManager: CommandManager = EditModule.commandManager
) : AbstractApplicationAction("file.action.save", application) {

    init {
        enabled = false
        eventBus.register(CommandEvent::class) { update() }
	    eventBus.register(CurrentSavableEvent::class) { update() }
    }

	override fun execute(event: ActionEvent) {
		if (application.data?.savable != null && application.data!!.savable.defined) {
			application.save()
		} else {
			application.saveAs()
		}
	}

    private fun update() {
        enabled = application.data == null || !application.data!!.savable.defined || commandManager.canUndo()
    }
}