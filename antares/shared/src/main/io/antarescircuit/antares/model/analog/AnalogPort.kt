package io.antarescircuit.antares.model.analog

import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.port.PortImpl

class AnalogPort(
	portType: PortType = PortType.INOUT,
	name: String? = null,
	description: TranslatableText = TranslatableText()
) : PortImpl<AnalogSignal>(portType, name, description) {

	/** The electrical current (in A) flowing through this [AnalogPort] during simulation. */
	var current: Double = 0.0

	/** Called by [AnalogNet] when its signal has changed.*/
	fun handleAnalogSignalChanged(signal: AnalogSignal?, signalHandler: SignalHandler) {
		(owner as? AnalogVertice)?.let {
			_incomingSignal = signal
			_outgoingSignal = signal
			it.handleAnalogPortChanged(this, signalHandler)
		}
	}

	override fun getDefaultSignal(): AnalogSignal = AnalogSignal.ZERO_VOLTAGE
}