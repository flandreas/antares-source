package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Module definitions for the [jabbah.draw.view] package.
 */
object DrawViewModule : AbstractModule() {

    var viewManager: ViewManager = ViewManagerImpl(BaseModule.eventBus)

    override fun initialize() {
        // empty so far
    }
}