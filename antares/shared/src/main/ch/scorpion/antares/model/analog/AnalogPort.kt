package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.PortImpl

class AnalogPort(
	portType: PortType = PortType.INOUT,
	name: String? = null
) : PortImpl<AnalogSignal>(portType, name) {
}