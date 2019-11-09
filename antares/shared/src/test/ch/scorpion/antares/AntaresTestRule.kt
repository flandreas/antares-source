package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.app.user.User
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.graph.module.GraphModuleJvm

/**
 * Basic setup of unit test in the [ch.scorpion.antares] package.
 */
object AntaresTestRule {

    fun configure() {
	    BaseModuleJvm.require()
	    AppModule.userHolder.u = User.developer()
        GraphModuleJvm.require()
        AntaresViewModule.require()
        AntaresThemes.install()
	    TestTranslationsBuilder().withAnyKey()
    }
}