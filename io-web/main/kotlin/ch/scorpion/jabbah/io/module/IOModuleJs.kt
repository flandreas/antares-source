package ch.scorpion.jabbah.io.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableClonerJs

/**
 * Module definitions for the [ch.scorpion.jabbah.io] module on the JS platform.
 */
object IOModuleJs : AbstractModule() {

	private val storableCloner: StorableCloner by lazy { StorableClonerJs() }

    override fun initialize() {
        BaseModuleJs.require()
        IOModule.require()
        IOModule.storableClonerProvider = { storableCloner }
    }
}