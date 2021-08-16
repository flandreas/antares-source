package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.base.event.EventBus

actual class ScriptEngine actual constructor(eventBus: EventBus) {

	actual val isSupported: Boolean get() = false

	actual fun eval(script: Script) {
		// not yet supported
	}

	actual fun invoke(name: String, script: Script?, errorHandler: ScriptErrorHandler?, vararg args: Any): Any? {
		// not yet supported
		return null
	}
}