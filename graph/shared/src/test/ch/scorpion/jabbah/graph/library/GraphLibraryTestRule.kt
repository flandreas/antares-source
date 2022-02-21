package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.DesktopUser
import ch.scorpion.jabbah.edit.auth.DesktopUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.IOModule

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.graph.library] package.
 */
object GraphLibraryTestRule {

	fun configure() {
		BaseModule.require()
		IOModule.require()
		GraphViewModule.require()
		Translations.withAnyKey()
		EditAuthModule.userHolder = DesktopUserHolder(DesktopUser.developer)
	}
}
