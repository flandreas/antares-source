package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.base.IssueImpl
import ch.scorpion.jabbah.base.IssueSeverity
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.AbstractExecutionError
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.net.SignalConflict
import ch.scorpion.jabbah.graph.model.net.SignalConflictBehaviour

/** Signals that a [PortImpl] tried to assign a signal to its [Net] that turns the [Net] inconsistent.*/
class InconsistentNetError(
	originPort: OutputPort<*>,
	private val conflict: SignalConflict<*>,
	creationTime: Long
) : AbstractExecutionError(creationTime, gracePeriod = BaseModule.properties.getInt(PROP_ALLOWED_DURATION)) {

	companion object {

		/** The name of the [Int] property in [Properties] representing the allowed duration (in ns) for inconsistent net states.*/
		const val PROP_ALLOWED_DURATION = "graph.model.allowedInconsistentNetDuration"

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