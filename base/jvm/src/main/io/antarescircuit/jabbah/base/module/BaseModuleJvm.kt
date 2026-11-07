package io.antarescircuit.jabbah.base.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.invocation.SynchronousInvocationHandler
import io.antarescircuit.jabbah.base.time.RealTimeServiceJvm

/** JVM services for core. Desktop setup lives in BaseModuleUiJvm in base-ui. */
object BaseModuleJvm : AbstractModule() {

	override fun initialize() {
		InvocationHandler.implementation = SynchronousInvocationHandler()
		BaseModule.timeService = RealTimeServiceJvm()
		BaseModule.require()
	}

	override fun resetDependencies() {
		BaseModule.reset()
	}
}
