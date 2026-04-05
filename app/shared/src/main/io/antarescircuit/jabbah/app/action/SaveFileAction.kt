package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.ApplicationDataViewController
import io.antarescircuit.jabbah.app.CurrentSavableEvent
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.CommandEvent
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.io.Storable

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
	    controller.addPropertyChangeListener(::savableChanged)
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
		    enabled = data != null && controller.isSavable && data!!.savable.defined && commandManager.canUndo()
		    description = Translations.getString("file.action.save.desc", commandManager.commandCount)
	    }
    }

	private fun savableChanged(event: PropertyChangeEvent<*>) {
		if (event.name == ApplicationDataViewController.PROP_SAVABLE) {
			update()
		}
	}
}