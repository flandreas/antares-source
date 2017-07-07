package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.graph.view] package.
 */
class GraphViewTestRule : TestRule {

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

    fun configure() {
        BaseModuleJvm.require()
        GraphViewModuleJvm.require()
    }
}