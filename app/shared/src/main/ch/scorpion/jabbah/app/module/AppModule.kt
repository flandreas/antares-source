package ch.scorpion.jabbah.app.module

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * Module definitions for the [ch.scorpion.jabbah.app] module.
 */
object AppModule : AbstractModule() {

    override fun initialize() {
        Translations.addBundle("jabbah-app")

        // TODO Refactor modularization. The app module depends on the edit module because AbstractDesktopApplication
        // creates an ApplicationFrame with an Editor. Make the app module independent of edit and provide
        // something like an "EditApplication" in the edit module.
        EditModule.require()

        /**
         * Reset the [CommandManager] whenever the current [Savable] of the [Application] has changed,
         * which is also the case if the [Savable] has been saved.
         */
        EditModule.commandManager.eventBus.register(CurrentSavableEvent::class) {
	        EditModule.commandManager.reset()
        }
    }
}