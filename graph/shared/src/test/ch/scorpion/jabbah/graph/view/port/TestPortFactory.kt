package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.graph.container.PortViewComponent
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.port.SubGraphPortImpl

/** A [PortFactory] implementation used for testing. */
class TestPortFactory : PortFactory {

	override fun <T : Any> createPort(portType: PortType): Port<T> {
		return PortImpl(portType, Boolean::class) as Port<T>
	}

	override fun <T : Any> createSubGraphPort(graphPort: GraphPort<T>): Port<T> {
		return SubGraphPortImpl(graphPort.portType, Boolean::class, graphPort.name!!) as Port<T>
	}

	override fun <T : Any> createPortView(port: Port<T>): PortView<T> {
		return TestPortView(port, Direction.WEST, PortLabelPosition.EXTERNAL, 0)
	}

	override fun <T : Any> createPortViewComponent(portView: PortView<T>): PortViewComponent<T> {
		return PortViewComponent(DrawStyleModule.styleProvider, portView)
	}

	override fun <T : Any> createOscilloscopeProbePort(name: String?): InputPort<T> {
		return PortImpl(PortType.INPUT)
	}
}