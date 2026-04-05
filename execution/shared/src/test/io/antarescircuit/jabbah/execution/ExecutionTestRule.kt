package io.antarescircuit.jabbah.execution

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.module.ExecutionModule

/**
 * Basic setup of unit tests in the [io.antarescircuit.jabbah.execution] package.
 */
object ExecutionTestRule {

	fun configure() {
		ExecutionModule.reset()

		//BaseModule.require()
		ExecutionModule.require()
		Translations.withAnyKey()
	}
}