package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.app.module.AppModule

/**
 * Basic setup of unit tests in the [io.antarescircuit.jabbah.app] package.
 */
object AppTestRule {

	fun configure() {
		AppModule.reset()

		//BaseModule.require()
		AppModule.require()
	}
}