package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.base.logger
import javax.script.Invocable
import javax.script.ScriptEngineManager

/**
 * Implements [ScriptEngine] using the JVM "nashorn" engine.
 */
class ScriptEngineJvm : ScriptEngine {

    private val LOG by logger(ScriptEngineJvm::class)
    private val engine = ScriptEngineManager().getEngineByName("nashorn")
    private var lastScript: String? = null

    override fun eval(script: String) {
        lastScript = script
        engine.eval(script)
    }

    override fun invoke(name: String, vararg args: Any): Any? {
        try {
            return (engine as Invocable).invokeFunction(name, *args)
        } catch (e: Throwable) {
            LOG.error("Error while invoking JS function '$name': ${e.message}")
            LOG.error("Invoked '$name' for the following script\n: $lastScript")
            throw e
        }
    }
}