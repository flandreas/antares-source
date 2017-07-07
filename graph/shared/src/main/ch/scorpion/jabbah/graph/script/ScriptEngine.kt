package ch.scorpion.jabbah.graph.script

/**
 * An interface to the scripting engine provided by the target platform.
 */
interface ScriptEngine {

    /** Evaluates (executes) the specified javascript. Primarily used for defining functions.*/
    fun eval(script: String)

    /** Invokes the function with the specified name and provides the given arguments.*/
    fun invoke(name: String, vararg args: Any): Any?
}