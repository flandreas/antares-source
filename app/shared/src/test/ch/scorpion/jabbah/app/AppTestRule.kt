package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.app] package.
 */
object AppTestRule {

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
        BaseModuleJvm.require()
        AppModule.require()
    }
}