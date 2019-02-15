package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.edit.module.EditModuleJvm

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.edit] package.
 */
object EditTestRule  {

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
        EditModuleJvm.require()
	    TestTranslationsBuilder().withAnyKey()
    }
}