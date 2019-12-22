package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.base.event.EventBus

actual class ScriptEngine actual constructor(eventBus: EventBus) {

	actual fun eval(script: Script) {
		throw UnsupportedOperationException("not implemented")
	}

	actual fun invoke(name: String, errorHandler: ScriptErrorHandler?, vararg args: Any): Any? {
		throw UnsupportedOperationException("not implemented")
	}
}