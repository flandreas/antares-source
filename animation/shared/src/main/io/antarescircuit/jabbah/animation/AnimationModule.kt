package io.antarescircuit.jabbah.animation

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.SystemSpeed

/**
 * Module definitions for the [io.antarescircuit.jabbah.animation] module.
 */
object AnimationModule : io.antarescircuit.jabbah.base.AbstractModule() {

	val constantSpeedAnimator = _root_ide_package_.io.antarescircuit.jabbah.animation.AnimatorImpl(
        _root_ide_package_.io.antarescircuit.jabbah.base.time.SystemSpeed(_root_ide_package_.io.antarescircuit.jabbah.base.time.SystemSpeed.MAX_SPEED),
        _root_ide_package_.io.antarescircuit.jabbah.base.System.createTimer()
    )

	override fun initialize() {
		configureProperties(_root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModule.properties)
	}

	override fun resetDependencies() {}

	private fun configureProperties(@Suppress("UNUSED_PARAMETER") properties: io.antarescircuit.jabbah.base.Properties) {
		// empty
	}
}