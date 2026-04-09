package io.antarescircuit.jabbah.animation

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.SystemSpeed

/**
 * Module definitions for the [io.antarescircuit.jabbah.animation] module.
 */
object AnimationModule : AbstractModule() {

	val constantSpeedAnimator = AnimatorImpl(
		SystemSpeed(SystemSpeed.MAX_SPEED),
        System.createTimer()
    )

	override fun initialize() {
		configureProperties(BaseModule.properties)
	}

	override fun resetDependencies() {}

	private fun configureProperties(@Suppress("UNUSED_PARAMETER") properties: Properties) {
		// empty
	}
}