package ch.scorpion.antares

import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.DigitalPortViewFactory
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.edit.auth.DesktopUser
import ch.scorpion.jabbah.edit.auth.DesktopUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.nonvolatile.EmptyNonVolatileService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * Basic setup of unit test in the [ch.scorpion.antares] package.
 */
object AntaresTestRule {

	fun configure() {
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