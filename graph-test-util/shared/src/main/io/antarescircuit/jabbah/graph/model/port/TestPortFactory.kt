package io.antarescircuit.jabbah.graph.model.port

import io.antarescircuit.jabbah.graph.model.GraphPort
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.model.InputPort
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.PortType

/** A [PortFactory] implementation used for testing. */
class TestPortFactory : PortFactory {

	override fun <T : Any> createSubGraphPort(graphPort: GraphPort<T>, type: GraphType): Port<T> =
		SubGraphPortImpl(graphPort.portType, graphPort.name!!)

	override fun <T : Any> createOscilloscopeProbePort(name: String?, type: GraphType): InputPort<T> =
		PortImpl(PortType.INPUT, name)
}