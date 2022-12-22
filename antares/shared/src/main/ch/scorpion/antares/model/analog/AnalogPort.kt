package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.PortImpl

class AnalogPort(
	name: String? = null
) : PortImpl<AnalogSignal>(PortType.INOUT, name) {
}