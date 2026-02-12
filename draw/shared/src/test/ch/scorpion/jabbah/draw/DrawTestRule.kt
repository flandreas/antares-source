package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.draw] package.
 */
object DrawTestRule {

	fun configure() {
		DrawModule.reset()

		//BaseModule.require()
		DrawModule.require()
	}
}