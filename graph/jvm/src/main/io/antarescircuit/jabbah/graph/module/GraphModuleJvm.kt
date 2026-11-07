package io.antarescircuit.jabbah.graph.module

import io.antarescircuit.jabbah.app.module.AppModuleJvm
import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.draw.module.DrawModuleJvm
import io.antarescircuit.jabbah.execution.ExecutionModuleJvm

/** JVM bootstrap for the graph core. Swing registrations live in graph-ui. */
object GraphModuleJvm : AbstractModule() {

	override fun initialize() {
		BaseModuleJvm.require()
		DrawModuleJvm.require()
		ExecutionModuleJvm.require()
		AppModuleJvm.require()
	}

	override fun resetDependencies() {
		BaseModuleJvm.reset()
		DrawModuleJvm.reset()
		ExecutionModuleJvm.reset()
		AppModuleJvm.reset()
	}
}
