package io.antarescircuit.antares.model.signal

import io.antarescircuit.antares.model.net.DigitalNet
import io.antarescircuit.jabbah.base.IssueImpl
import io.antarescircuit.jabbah.base.IssueSeverity
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.AbstractExecutionError
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.OutputPort

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