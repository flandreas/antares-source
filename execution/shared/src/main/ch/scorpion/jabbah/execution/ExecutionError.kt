package ch.scorpion.jabbah.execution

interface ExecutionError {
	fun reevaluate(signalHandler: SignalHandler)
}