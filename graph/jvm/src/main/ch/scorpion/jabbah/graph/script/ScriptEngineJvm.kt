package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.issue.IssueImpl
import ch.scorpion.jabbah.execution.issue.IssueSeverity
import javax.script.Invocable
import javax.script.ScriptEngineManager
import javax.script.ScriptException

/**
 * Implements [ScriptEngine] using the JVM "nashorn" engine.
 */
actual class ScriptEngine actual constructor(private val eventBus: EventBus) {

	companion object {
		private val LOG by logger(ScriptEngine::class)
	}

	private val engine = ScriptEngineManager().getEngineByName("nashorn")
	private var lastScript: Script? = null

	actual fun eval(script: Script) {
		lastScript = script
		try {
			engine.eval(script.code)
		} catch (e: ScriptException) {
			LOG.trace("ScriptException while defining JS function: '${e.message}'")
			postIssue(script, e)
		} catch (e: Throwable) {
			LOG.error("General error while defining JS function: '${e.message}'")
			throw e
		}
	}

	actual fun invoke(name: String, script: Script?, errorHandler: ScriptErrorHandler?, vararg args: Any): Any? {
		return try {
			(engine as Invocable).invokeFunction(name, *args)
		} catch (e: ScriptException) {
			LOG.trace("ScriptException while invoking JS function: '${e.message}'")
			postIssue(script ?: lastScript!!, e)
			null
		} catch (e: Throwable) {
			if (errorHandler != null && !errorHandler.errorHandled) {
				LOG.error("Error while invoking JS function '$name': ${e.message}")
				LOG.trace("Invoked '$name' for the following script\n: ${script ?: lastScript}")
				throw e
			}
			return null
		}
	}

	private fun postIssue(script: Script, e: ScriptException) {
		eventBus.post(IssueImpl(
			severity = IssueSeverity.Error,
			name = "JS Script",
			description = "${e.message}",
			origin = script.origin,
			context = script.context))
	}
}