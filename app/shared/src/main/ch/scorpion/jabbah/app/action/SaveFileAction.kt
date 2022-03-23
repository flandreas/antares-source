package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandEvent
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.io.Storable

/**
 * An [Action] for saving the current [Storable] with the current file path.
 */
class SaveFileAction(
	private val controller: ApplicationDataViewController,
	private val eventBus: EventBus = BaseModule.eventBus,
	val commandManager: CommandManager = EditModule.commandManager
) : AbstractAction("file.action.save") {

	private val commandEventHandler: EventHandler<CommandEvent> = { update() }
	private val currentSavableHandler: EventHandler<CurrentSavableEvent> = { update() }

    init {
        enabled = false
        eventBus.register(CommandEvent::class, commandEventHandler)
	    eventBus.register(CurrentSavableEvent::class, currentSavableHandler)
    }

	override fun dispose() {
		super.dispose()
		eventBus.unregister(commandEventHandler)
		eventBus.unregister(currentSavableHandler)
	}

	override fun execute(event: ActionEvent) {
		controller.apply {
			if (data?.savable != null && data!!.savable.defined) {
				save()
			} else {
				saveAs()
			}
		}
	}

    private fun update() {
	    controller.apply {
		    enabled = data == null || !data!!.savable.defined || commandManager.canUndo()
		    description = Translations.getString("file.action.save.desc", commandManager.commandCount)
	    }
    }
}