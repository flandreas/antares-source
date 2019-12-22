package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.app.user.User
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.io.IOModule

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.graph.library] package.
 */
object GraphLibraryTestRule {

	fun configure() {
		BaseModule.require()
		IOModule.require()
		LibraryModule.require()
		Translations.withAnyKey()
		AppModule.userHolder.u = User.developer()
	}
}
