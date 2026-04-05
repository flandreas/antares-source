package io.antarescircuit.jabbah.app.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.module.EditModule

/**
 * Module definitions for the [io.antarescircuit.jabbah.app] module.
 */
object AppModule : AbstractModule() {

    override fun initialize() {
        Translations.addBundle("jabbah-app")

        // TODO Refactor modularization. The app module depends on the edit module because AbstractDesktopApplication
        // creates an ApplicationFrame with an Editor. Make the app module independent of edit and provide
        // something like an "EditApplication" in the edit module.
        EditModule.require()
    }

    override fun resetDependencies() {
        EditModule.reset()
    }
}