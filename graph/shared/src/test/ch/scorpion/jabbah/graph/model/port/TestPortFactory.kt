package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType

/** A [PortFactory] implementation used for testing. */
class TestPortFactory : PortFactory {

	override fun <T : Any> createPort(portType: PortType): Port<T> {
		return PortImpl(portType, Boolean::class) as Port<T>
	}

	override fun <T : Any> createSubGraphPort(graphPort: GraphPort<T>): Port<T> {
		return SubGraphPortImpl(graphPort.portType, Boolean::class, graphPort.name!!) as Port<T>
	}

	override fun <T : Any> createOscilloscopeProbePort(name: String?): InputPort<T> {
		return PortImpl(PortType.INPUT)
	}
}