package io.antarescircuit.jabbah.edit.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.edit.model.text.EditModelTextModule
import io.antarescircuit.jabbah.edit.model.text.TextComponentFactoryJs
import io.antarescircuit.jabbah.io.module.IOModuleJs
import io.antarescircuit.jabbah.module.DrawModuleJs

/**
 * Module definitions for the [io.antarescircuit.jabbah.edit] package on the JavaScript platform.
 */
object EditModuleJs : AbstractModule() {

    override fun initialize() {
        IOModuleJs.require()
        DrawModuleJs.require()
        EditModule.require()

	    EditModelTextModule.textComponentFactory = TextComponentFactoryJs()
    }

    override fun resetDependencies() {
        IOModuleJs.reset()
        DrawModuleJs.reset()
        EditModule.reset()
    }
}