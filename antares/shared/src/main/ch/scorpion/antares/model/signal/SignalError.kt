package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.jabbah.base.IssueImpl
import ch.scorpion.jabbah.base.IssueSeverity
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.AbstractExecutionError
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.OutputPort

/** Set on a [DigitalNet] if the [DigitalSignal] to be forwarded contains a [Bit.Error].*/
class SignalError(
	originPort: OutputPort<*>,
	creationTime: Long
) : AbstractExecutionError(creationTime, gracePeriod = 0) {

	private val name = Translations.getString("digitalnet.signalError.name")
	private val originDesc = Translations.getString("digitalnet.signalError.origin", "${originPort.owner!!.type} (${originPort.owner!!.id})")

	override val tooltipText: String get() {
		val text = StringBuilder()
		text.append(name)
		text.appendLine()
		text.append(originDesc)
		return text.toString()
	}

	override fun reevaluated(force: Boolean, signalHandler: SignalHandler): Boolean {
		BaseModule.eventBus.post(IssueImpl(
			severity = IssueSeverity.Error,
			name,
			description = Translations.getString("digitalnet.signalError.desc"),
			origin = originDesc,
			context = null))
		return true
	}
}