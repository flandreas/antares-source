package ch.scorpion.antares.view.port

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.port.SubCircuitPort
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.port.PortFactory

class AntaresPortFactory : PortFactory {

    override fun <T : Any> createSubGraphPort(graphPort: GraphPort<T>, type: GraphType): Port<T> =
		when (type) {
			AntaresGraphTypes.Digital -> createDigitalSubGraphPort(graphPort)
		    AntaresGraphTypes.Analog -> createAnalogSubGraphPort(graphPort)
		    else -> throw IllegalArgumentException("Unsupported GraphType $type")
		}

    override fun <T : Any> createOscilloscopeProbePort(name: String?): InputPort<T> {
        val port = DigitalPortImpl.createInput(name)
        port.isAdaptive = true
        return port as InputPort<T>
    }

	private fun <T : Any> createDigitalSubGraphPort(graphPort: GraphPort<T>): Port<T> {
		val subCircuitPort = SubCircuitPort(graphPort.portType, graphPort.name)
		subCircuitPort.bitWidth = (graphPort as DigitalCircuitInOut).bitWidth
		subCircuitPort.signalRepresentation = graphPort.signalRepresentation
		return subCircuitPort as Port<T>
	}

	private fun <T : Any> createAnalogSubGraphPort(graphPort: GraphPort<T>): Port<T> =
		SubCircuitPort(graphPort.portType, graphPort.name) as Port<T>
}