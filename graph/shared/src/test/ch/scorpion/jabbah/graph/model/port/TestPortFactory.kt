package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.graph.model.*

/** A [PortFactory] implementation used for testing. */
class TestPortFactory : PortFactory {

	override fun <T : Any> createSubGraphPort(graphPort: GraphPort<T>, type: GraphType): Port<T> =
		SubGraphPortImpl(graphPort.portType, graphPort.name!!)

	override fun <T : Any> createOscilloscopeProbePort(name: String?, type: GraphType): InputPort<T> =
		PortImpl(PortType.INPUT, name)
}