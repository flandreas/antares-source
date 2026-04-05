package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.auth.DesktopUser
import io.antarescircuit.jabbah.edit.auth.DesktopUserHolder
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.io.IOModule

/**
 * Basic setup of unit tests in the [io.antarescircuit.jabbah.graph.library] package.
 */
object GraphLibraryTestRule {

	fun configure() {
		GraphViewModule.reset()

		BaseModule.require()
		IOModule.require()
		GraphViewModule.require()
		Translations.withAnyKey()
		EditAuthModule.userHolder = DesktopUserHolder(DesktopUser.developer)
	}
}
