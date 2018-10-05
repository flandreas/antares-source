package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Basic setup of unit test in the [ch.scorpion.antares] package.
 */
class AntaresTestRule : TestRule {

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

    private fun configure() {
        GraphModuleJvm.require()
        AntaresViewModule.require()
        AntaresThemes.install()
	    TestTranslationsBuilder().withAnyKey()
    }
}