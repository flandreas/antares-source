package ch.scorpion.jabbah.io.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.io.IOModule

/**
 * Module definitions for the [ch.scorpion.jabbah.io] module on the JS platform.
 */
object IOModuleJs : AbstractModule() {

    override fun initialize() {
        BaseModuleJs.require()
        IOModule.require()
    }
}