package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.app] package.
 */
object AppTestRule {

	fun configure() {
		BaseModule.require()
		AppModule.require()
	}
}