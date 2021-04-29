package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.ExecutionErrorHandler
import ch.scorpion.jabbah.execution.SignalHandler

class ExecutionErrorHandlerImpl : ExecutionErrorHandler {

	/**
	 * Collects [ExecutionError] received by [deferExecutionError] for reevaluation once the current
	 * execution cycle has ended.
	 */
	private val executionErrors = mutableListOf<ExecutionError>()

	override val executionErrorCount: Int get() = executionErrors.size

	override fun deferExecutionError(error: ExecutionError) {
		executionErrors.add(error)
	}

	fun reset() {
		executionErrors.clear()
	}

	fun reevaluateExecutionErrors(force: Boolean, signalHandler: SignalHandler) {
		if (executionErrors.isEmpty()) {
			return
		}
		val toRemove = mutableListOf<ExecutionError>()
		executionErrors.forEach {
			if (it.reevaluated(force, signalHandler)) {
				toRemove.add(it)
			}
		}
		executionErrors.removeAll(toRemove)
	}
}