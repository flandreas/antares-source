package io.antarescircuit.jabbah.draw

import io.antarescircuit.jabbah.draw.module.DrawModule

/**
 * Basic setup of unit tests in the [io.antarescircuit.jabbah.draw] package.
 */
object DrawTestRule {

	fun configure() {
		DrawModule.reset()

		//BaseModule.require()
		DrawModule.require()
	}
}