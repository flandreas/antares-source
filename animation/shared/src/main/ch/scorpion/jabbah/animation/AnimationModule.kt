package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed

/**
 * Module definitions for the [ch.scorpion.jabbah.animation] module.
 */
object AnimationModule : AbstractModule() {

	val constantSpeedAnimator = AnimatorImpl(SystemSpeed(SystemSpeed.MAX_SPEED), System.createTimer())

	override fun initialize() {
		configureProperties(BaseModule.properties)
	}

	private fun configureProperties(@Suppress("UNUSED_PARAMETER") properties: Properties) {
		// empty
	}
}