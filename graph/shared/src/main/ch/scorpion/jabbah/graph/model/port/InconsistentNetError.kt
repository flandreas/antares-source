package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.logger
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
	private val conflict: SignalConflict<*>,
	override val creationTime: Long
) : ExecutionError {

	companion object {

		/** The name of the [Int] property in [Properties] representing the allowed duration (in ns) for inconsistent net states.*/
		const val PROP_ALLOWED_DURATION = "graph.model.allowedInconsistentNetDuration"
	}

	private val gracePeriod: Int = BaseModule.properties.getInt(PROP_ALLOWED_DURATION)

	override fun reevaluated(signalHandler: SignalHandler): Boolean {
		val executionTime = signalHandler.executionTime
		if (conflict.chain.hasExecutionError) {
			return if (isGracePeriodOver(executionTime)) {
				post()
				true
			} else {
				false
			}
		}
		return true
	}

	private fun getAge(executionTime: Long): Long = executionTime - creationTime

	private fun isGracePeriodOver(executionTime: Long): Boolean = getAge(executionTime) > gracePeriod

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