package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.execution] package.
 */
object ExecutionTestRule {

	fun configure() {
		BaseModule.require()
		ExecutionModule.require()
	}
}