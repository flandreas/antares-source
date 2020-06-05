package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * An interface to the scripting engine provided by the target platform.
 */
expect class ScriptEngine(eventBus: EventBus = BaseModule.eventBus) {

    /** Evaluates (executes) the specified javascript. Primarily used for defining functions.*/
    fun eval(script: Script)

	/** Invokes the function with the specified name and provides the given arguments.*/
    fun invoke(name: String, script: Script?, errorHandler: ScriptErrorHandler? = null, vararg args: Any): Any?
}

interface ScriptErrorHandler {

	/** Determines whether the current error has already been handled. */
	val errorHandled: Boolean
}