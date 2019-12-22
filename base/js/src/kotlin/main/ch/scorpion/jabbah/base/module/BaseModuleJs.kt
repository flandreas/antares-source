package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.*

/**
 * Setup of the [ch.scorpion.jabbah.base] module for the JavaScript target.
 */
object BaseModuleJs : AbstractModule() {

    override fun initialize() {
        BaseModule.require()
    }
}