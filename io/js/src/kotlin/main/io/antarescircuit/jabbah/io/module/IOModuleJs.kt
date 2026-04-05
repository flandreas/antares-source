package io.antarescircuit.jabbah.io.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.module.BaseModuleJs
import io.antarescircuit.jabbah.io.IOModule

/**
 * Module definitions for the [io.antarescircuit.jabbah.io] module on the JS platform.
 */
object IOModuleJs : AbstractModule() {

	override fun initialize() {
		BaseModuleJs.require()
		IOModule.require()
	}

	override fun resetDependencies() {
		BaseModuleJs.reset()
		IOModule.reset()
	}
}