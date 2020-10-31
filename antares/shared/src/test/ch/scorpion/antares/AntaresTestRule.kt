package ch.scorpion.antares

import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * Basic setup of unit test in the [ch.scorpion.antares] package.
 */
object AntaresTestRule {

	fun configure() {
		BaseModule.require()
		EditAuthModule.userHolder.u = User.developer
		GraphViewModule.require()
		AntaresViewModule.require()
		AntaresThemes.install()
		Translations.withAnyKey()
	}
}