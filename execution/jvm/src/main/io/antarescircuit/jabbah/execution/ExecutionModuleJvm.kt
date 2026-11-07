package io.antarescircuit.jabbah.execution

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.execution.module.ExecutionModule

/** JVM execution setup. Desktop preferences live in execution-ui. */
object ExecutionModuleJvm : AbstractModule() {

	override fun initialize() {
		BaseModuleJvm.require()
		ExecutionModule.require()
	}

	override fun resetDependencies() {
		BaseModuleJvm.reset()
		ExecutionModule.reset()
	}
}
