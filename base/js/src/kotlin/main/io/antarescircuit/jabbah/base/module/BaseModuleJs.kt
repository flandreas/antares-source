package io.antarescircuit.jabbah.base.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.TranslationServiceJs
import io.antarescircuit.jabbah.base.TranslationServiceJsImpl
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.invocation.InvocationHandlerJs
import io.antarescircuit.jabbah.base.time.RealTimeServiceJs

/**
 * Setup of the [io.antarescircuit.jabbah.base] module for the JavaScript target.
 */
object BaseModuleJs : AbstractModule() {

    var translationService: TranslationServiceJs = TranslationServiceJsImpl("http://localhost:8080/api")

    override fun initialize() {
        BaseModule.require()

	    InvocationHandler.implementation = InvocationHandlerJs()
	    BaseModule.timeService = RealTimeServiceJs()
    }

    override fun resetDependencies() {
        BaseModule.reset()
    }
}