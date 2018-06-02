package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.IOModuleJvm
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.graph.library] package.
 */
class GraphLibraryTestRule : TestRule {

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
		IOModuleJvm.require()
		LibraryModule.require()
	}
}
