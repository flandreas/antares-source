package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.CommandManagerActiveEvent
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * A base [Action] that is only enabled if the [CommandManager] is active.
 */
abstract class AbstractCommandManagerAwareAction(
	baseName: String,
	private val commandManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractViewAction(baseName, eventBus, viewManager) {

	init {
		eventBus.register(CommandManagerActiveEvent::class) { updateEnabled() }
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && commandManager.active
	}
}