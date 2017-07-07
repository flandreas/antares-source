package ch.scorpion.antares.view.port

import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.port.SubCircuitPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.container.DigitalPortViewComponent
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.container.PortViewComponent
import ch.scorpion.jabbah.graph.view.port.PortFactory
import ch.scorpion.jabbah.graph.view.port.PortView

class DigitalPortFactory(private val styleProvider: StyleProvider) : PortFactory {

    override fun <T : Any> createSubGraphPort(graphPort: GraphPort<T>): Port<T> {
        val subCircuitPort = SubCircuitPort(graphPort.portType, graphPort.name)
        subCircuitPort.bitWidth = (graphPort as CircuitInOut).bitWidth
        subCircuitPort.signalRepresentation = graphPort.signalRepresentation
        return subCircuitPort as Port<T>
    }

    override fun <T : Any> createPortView(port: Port<T>): PortView<T> {
        return when(port.portType) {
            PortType.INPUT -> DigitalPortView(
                styleProvider = styleProvider,
                port = port as Port<DigitalSignal>,
                direction = Direction.WEST)
            PortType.OUTPUT, PortType.INOUT -> DigitalPortView(
                styleProvider = styleProvider,
                port = port as Port<DigitalSignal>,
                direction = Direction.EAST)
        } as PortView<T>
    }

    override fun <T : Any> createPortViewComponent(portView: PortView<T>): PortViewComponent<T> {
        return DigitalPortViewComponent(styleProvider, portView as DigitalPortView) as PortViewComponent<T>
    }
}