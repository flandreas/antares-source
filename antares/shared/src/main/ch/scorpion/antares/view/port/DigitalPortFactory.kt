package ch.scorpion.antares.view.port

import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.port.SubCircuitPort
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.PortFactory

class DigitalPortFactory : PortFactory {

    override fun <T : Any> createPort(portType: PortType): Port<T> {
        return DigitalPortImpl.createPort(portType) as Port<T>
    }

    override fun <T : Any> createSubGraphPort(graphPort: GraphPort<T>): Port<T> {
        val subCircuitPort = SubCircuitPort(graphPort.portType, graphPort.name)
        subCircuitPort.bitWidth = (graphPort as CircuitInOut).bitWidth
        subCircuitPort.signalRepresentation = graphPort.signalRepresentation
        return subCircuitPort as Port<T>
    }

    override fun <T : Any> createOscilloscopeProbePort(name: String?): InputPort<T> {
        val port = DigitalPortImpl.createInput(name)
        port.isAdaptive = true
        return port as InputPort<T>
    }
}