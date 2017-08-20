package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.io.IOModuleJvm
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Basic setup of unit test in the [antares] package.
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
    }
}