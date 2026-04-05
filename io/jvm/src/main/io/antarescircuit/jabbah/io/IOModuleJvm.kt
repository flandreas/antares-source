package io.antarescircuit.jabbah.io

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.module.BaseModuleJvm

/**
 * Module definitions for the [io.antarescircuit.jabbah.io] module on the JVM platform.
 */
object IOModuleJvm : AbstractModule() {

    override fun initialize() {
        BaseModuleJvm.require()
        IOModule.require()
    }

    override fun resetDependencies() {
        BaseModuleJvm.reset()
        IOModule.reset()
    }
}