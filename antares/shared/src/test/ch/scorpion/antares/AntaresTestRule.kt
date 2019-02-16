package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.app.user.User
import ch.scorpion.jabbah.graph.module.GraphModuleJvm

/**
 * Basic setup of unit test in the [ch.scorpion.antares] package.
 */
object AntaresTestRule {

	/*
    override fun apply(statement: Statement?, p1: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                configure()
                try {
                    statement!!.evaluate()
                } finally {
                    // empty
                }
            }
        }
    }
    */

    fun configure() {
        GraphModuleJvm.require()
        AntaresViewModule.require()
        AntaresThemes.install()
	    TestTranslationsBuilder().withAnyKey()
	    AppModule.userHolder.u = User.developer()
    }
}