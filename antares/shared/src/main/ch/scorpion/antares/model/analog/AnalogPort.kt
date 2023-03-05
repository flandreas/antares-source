package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.PortImpl

class AnalogPort(
	portType: PortType = PortType.INOUT,
	name: String? = null
) : PortImpl<AnalogSignal>(portType, name) {

	/** Called by [AnalogNet] when its signal has changed.*/
	fun handleAnalogSignalChanged(signal: AnalogSignal?, signalHandler: SignalHandler) {
		(owner as? AnalogVertice)?.let {
			_incomingSignal = signal
			_outgoingSignal = signal
			it.handleAnalogPortChanged(this, signalHandler)
		}
	}
}