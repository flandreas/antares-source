package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.issue.IssueImpl
import ch.scorpion.jabbah.execution.issue.IssueSeverity
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.net.SignalConflict
import ch.scorpion.jabbah.graph.model.net.SignalConflictBehaviour

/** Signals that a [PortImpl] tried to assign a signal to its [Net] that turns the [Net] inconsistent.*/
class InconsistentNetError(
	originPort: OutputPort<*>,
	private val conflict: SignalConflict<*>
) : ExecutionError {

	override fun reevaluate(signalHandler: SignalHandler) {
		if (conflict.chain.hasExecutionError) {
			post()
		}
	}

	override val tooltipText: String get() {
		val text = StringBuilder()
		text.append(name)
		text.append("<p/>")
		text.append(originDesc)
		text.append("<p/>")
		text.append(description)
		return text.toString()
	}

	private val name: String get() = Translations.getString("graph.inconsistentNetError.name")

	private val description = Translations.getString(
		"graph.inconsistentNetError.description",
		"${conflict.convertedSignal}, ${conflict.chain.destinationOutputPort.getOutgoingSignal()}")

	private val originDesc = Translations.getString(
		"graph.inconsistentNetError.origin",
		"${originPort.owner!!.type} (${originPort.owner!!.id})",
		"${conflict.chain.destinationOutputPort.owner!!.type} (${conflict.chain.destinationOutputPort.owner!!.id})")

	private fun post() {
		val severity = when (GraphModelModule.signalConflictBehaviourHolder.current) {
			SignalConflictBehaviour.IGNORE -> return
			SignalConflictBehaviour.ISSUE_WARNING -> IssueSeverity.Warning
			SignalConflictBehaviour.ISSUE_ERROR -> IssueSeverity.Error
		}

		BaseModule.eventBus.post(IssueImpl(
			severity,
			name,
			description = description,
			origin = originDesc,
			context = null
		))
	}
}