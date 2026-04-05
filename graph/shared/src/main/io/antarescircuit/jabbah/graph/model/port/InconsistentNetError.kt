package io.antarescircuit.jabbah.graph.model.port

import io.antarescircuit.jabbah.base.IssueImpl
import io.antarescircuit.jabbah.base.IssueSeverity
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.AbstractExecutionError
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.model.OutputPort
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.net.SignalConflict
import io.antarescircuit.jabbah.graph.model.net.SignalConflictBehaviour
import io.antarescircuit.jabbah.graph.library.LibraryPreferences

/** Signals that a [PortImpl] tried to assign a signal to its [Net] that turns the [Net] inconsistent.*/
class InconsistentNetError(
	originPort: OutputPort<*>,
	private val conflict: SignalConflict<*>,
	creationTime: Long
) : AbstractExecutionError(
		creationTime,
		gracePeriod = LibraryPreferences.getInt(PROP_ALLOWED_DURATION)
) {

	companion object {

		/**
		 * The name of the [Int] property in [Properties] representing the allowed duration (in ns) for inconsistent net states.
		 * Also stored locally in [LibraryPreferences] to be independent of base [Properties].
		 */
		const val PROP_ALLOWED_DURATION = "graph.model.allowedInconsistentNetDuration"

		const val DEF_ALLOWED_DURATION = 20

		private val NAME by lazy { Translations.getString("graph.inconsistentNetError.name") }
	}

	private val description = Translations.getString(
		"graph.inconsistentNetError.description",
		"${conflict.signal}, ${conflict.destinationPort.getOutgoingSignal()}")

	private val originDesc = Translations.getString(
		"graph.inconsistentNetError.origin",
		"${originPort.owner!!.type} (${originPort.owner!!.id})",
		"${conflict.destinationPort.owner!!.type} (${conflict.destinationPort.owner!!.id})")

	override fun reevaluated(force: Boolean, signalHandler: SignalHandler): Boolean {
		val executionTime = signalHandler.executionTime
		if (conflict.combinedNet.hasExecutionError) {
			return if (force || isGracePeriodOver(executionTime)) {
				post()
				true
			} else {
				false
			}
		}
		return true
	}

	override val tooltipText: String get() {
		val text = StringBuilder()
		text.append(NAME)
		text.appendLine()
		text.append(originDesc)
		text.appendLine()
		text.append(description)
		return text.toString()
	}

	private fun post() {
		val severity = when (GraphModelModule.signalConflictBehaviourHolder.current) {
			SignalConflictBehaviour.IGNORE -> return
			SignalConflictBehaviour.ISSUE_WARNING -> IssueSeverity.Warning
			SignalConflictBehaviour.ISSUE_ERROR -> IssueSeverity.Error
		}

		BaseModule.eventBus.post(IssueImpl(
			severity,
			NAME,
			description = description,
			origin = originDesc,
			context = null
		))
	}
}