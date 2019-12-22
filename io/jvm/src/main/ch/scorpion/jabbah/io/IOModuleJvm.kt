package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm

/**
 * Module definitions for the [ch.scorpion.jabbah.io] module on the JVM platform.
 */
object IOModuleJvm : AbstractModule() {

    override fun initialize() {
        BaseModuleJvm.require()
        IOModule.require()
    }
}