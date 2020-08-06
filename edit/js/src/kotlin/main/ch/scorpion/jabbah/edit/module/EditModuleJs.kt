package ch.scorpion.jabbah.edit.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.edit.model.text.TextComponentFactoryJs
import ch.scorpion.jabbah.io.module.IOModuleJs
import ch.scorpion.jabbah.module.DrawModuleJs

actual object EditModuleAccess {

	actual fun require() {
		EditModuleJs.require()
	}
}

/**
 * Module definitions for the [ch.scorpion.jabbah.edit] package on the JavaScript platform.
 */
object EditModuleJs : AbstractModule() {

    override fun initialize() {
        IOModuleJs.require()
        DrawModuleJs.require()
        EditModule.require()
    }
}