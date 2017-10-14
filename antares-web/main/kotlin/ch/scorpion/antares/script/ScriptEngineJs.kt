package ch.scorpion.antares.script

import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.script.ScriptEngine

/**
 * Implements [ScriptEngine] on the JavaScript platform.
 * // TODO Refactor: Move to ch.scorpion.jabbah.graph module
 */
class ScriptEngineJs : ScriptEngine {

    override fun eval(script: Script) {
        // TODO Argument of js() must be a constant String??
        js("TODO")
    }

    override fun invoke(name: String, vararg args: Any): Any? {
        // TODO
        return null
    }
}