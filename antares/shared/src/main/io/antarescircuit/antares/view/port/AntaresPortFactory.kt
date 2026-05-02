package io.antarescircuit.antares.view.port

import io.antarescircuit.antares.model.AntaresGraphTypes.Analog
import io.antarescircuit.antares.model.AntaresGraphTypes.Digital
import io.antarescircuit.antares.model.analog.AnalogPort
import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.port.SubCircuitPort
import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.model.port.PortFactory

@Suppress("UNCHECKED_CAST")
class AntaresPortFactory : PortFactory {

    override fun <T : Any> createSubGraphPort(graphPort: GraphPort<T>, type: GraphType): Port<T> =
		when (type) {
			Digital -> createDigitalSubGraphPort(graphPort)
		    Analog -> createAnalogSubGraphPort(graphPort)
		    else -> throw IllegalArgumentException("Unsupported GraphType $type")
		}

    override fun <T : Any> createOscilloscopeProbePort(name: String?, type: GraphType): InputPort<T> {
	    val port = when (type) {
			Digital, GenericGraphType -> DigitalPortImpl.createInput(name).also { it.isAdaptive = true }
		    Analog -> AnalogPort(PortType.INPUT, name)
		    else -> throw IllegalArgumentException("Unsupported GraphType $type")
		}
        return port as InputPort<T>
    }

	private fun <T : Any> createDigitalSubGraphPort(graphPort: GraphPort<T>): Port<T> {
		val subCircuitPort = SubCircuitPort(graphPort.portType, graphPort.name)
		subCircuitPort.bitWidth = (graphPort as DigitalCircuitInOut).bitWidth
		subCircuitPort.signalRepresentation = graphPort.signalRepresentation
		subCircuitPort.unconnectedStartValue = (graphPort as DigitalCircuitInOut).startValue
		return subCircuitPort as Port<T>
	}

	private fun <T : Any> createAnalogSubGraphPort(graphPort: GraphPort<T>): Port<T> =
		SubCircuitPort(graphPort.portType, graphPort.name) as Port<T>
}