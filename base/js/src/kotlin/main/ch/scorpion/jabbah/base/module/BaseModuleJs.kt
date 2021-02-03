package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandlerJs
import ch.scorpion.jabbah.base.time.RealTimeServiceJs

/**
 * Setup of the [ch.scorpion.jabbah.base] module for the JavaScript target.
 */
object BaseModuleJs : AbstractModule() {

	var translationService: TranslationService = TranslationServiceImpl()

    override fun initialize() {
        BaseModule.require()

	    InvocationHandler.implementation = InvocationHandlerJs()
	    BaseModule.timeService = RealTimeServiceJs()
    }
}