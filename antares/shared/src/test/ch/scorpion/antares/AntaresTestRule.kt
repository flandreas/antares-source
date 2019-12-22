package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.app.user.User
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * Basic setup of unit test in the [ch.scorpion.antares] package.
 */
object AntaresTestRule {

	fun configure() {
		BaseModule.require()
		AppModule.userHolder.u = User.developer()
		GraphViewModule.require()
		AntaresViewModule.require()
		AntaresThemes.install()
		Translations.withAnyKey()
	}
}