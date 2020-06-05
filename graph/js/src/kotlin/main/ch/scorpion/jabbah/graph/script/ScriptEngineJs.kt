package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.base.event.EventBus

actual class ScriptEngine actual constructor(eventBus: EventBus) {

	actual fun eval(script: Script) {
		throw UnsupportedOperationException()
	}

	actual fun invoke(name: String, script: Script?, errorHandler: ScriptErrorHandler?, vararg args: Any): Any? {
		throw UnsupportedOperationException()
	}
}