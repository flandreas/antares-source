package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType

/** A [PortFactory] implementation used for testing. */
class TestPortFactory : PortFactory {

	override fun <T : Any> createSubGraphPort(graphPort: GraphPort<T>, type: GraphType): Port<T> =
		SubGraphPortImpl(graphPort.portType, graphPort.name!!)

	override fun <T : Any> createOscilloscopeProbePort(name: String?, type: GraphType): InputPort<T> =
		PortImpl(PortType.INPUT, name)
}