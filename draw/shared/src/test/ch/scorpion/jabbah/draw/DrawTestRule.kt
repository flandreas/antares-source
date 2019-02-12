package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.module.DrawModuleJvm

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.draw] package.
 */
object DrawTestRule {

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
        DrawModuleJvm.require()
    }
}