package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.draw] package.
 */
object DrawTestRule {

	fun configure() {
		BaseModule.require()
		DrawModule.require()
	}
}