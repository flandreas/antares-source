package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Module definitions for the [jabbah.animation] module.
 */
object AnimationModule : AbstractModule() {

    var animator: Animator = AnimatorImpl(System.get().createTimer())

    override fun initialize() {
        configureProperties(BaseModule.properties)
    }

    private fun configureProperties(@Suppress("UNUSED_PARAMETER") properties: Properties) {
        // empty
    }
}