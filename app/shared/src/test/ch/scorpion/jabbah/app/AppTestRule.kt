package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.module.AppModule

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.app] package.
 */
object AppTestRule {

	fun configure() {
		AppModule.reset()

		//BaseModule.require()
		AppModule.require()
	}
}