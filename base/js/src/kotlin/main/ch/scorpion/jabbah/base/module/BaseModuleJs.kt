package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.TranslationServiceJs
import ch.scorpion.jabbah.base.TranslationServiceJsImpl
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandlerJs
import ch.scorpion.jabbah.base.time.RealTimeServiceJs

/**
 * Setup of the [ch.scorpion.jabbah.base] module for the JavaScript target.
 */
object BaseModuleJs : AbstractModule() {

    const val AKRAB_URL = "http://localhost:8080"

    var translationService: TranslationServiceJs = TranslationServiceJsImpl(AKRAB_URL)

    override fun initialize() {
        BaseModule.require()

	    InvocationHandler.implementation = InvocationHandlerJs()
	    BaseModule.timeService = RealTimeServiceJs()
    }
}