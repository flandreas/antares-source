package io.antarescircuit.antares

import io.antarescircuit.antares.view.module.AntaresViewModule
import io.antarescircuit.antares.view.port.DigitalPortViewFactory
import io.antarescircuit.antares.view.theme.AntaresThemes
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.edit.auth.DesktopUser
import io.antarescircuit.jabbah.edit.auth.DesktopUserHolder
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.nonvolatile.EmptyNonVolatileService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

/**
 * Basic setup of unit test in the [io.antarescircuit.antares] package.
 */
object AntaresTestRule {

	fun configure() {
		AntaresViewModule.reset()

		BaseModule.require()
		EditAuthModule.userHolder = DesktopUserHolder(DesktopUser.developer)

		GraphViewModule.require()
		GraphModelModule.nonVolatileService = EmptyNonVolatileService()
		GraphViewModule.portViewFactory = DigitalPortViewFactory(DrawStyleModule.styleProvider)

		AntaresViewModule.require()
		AntaresThemes.install()
		Translations.withAnyKey()
	}
}