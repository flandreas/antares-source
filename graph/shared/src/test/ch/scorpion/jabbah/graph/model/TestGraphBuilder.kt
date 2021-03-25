package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.graph.model.net.NetImpl

class TestGraphBuilder<T: Any>(
	eventBus: EventBus = BaseModule.eventBus) {

	val graph = GraphImpl(eventBus = eventBus)

	fun <V : Vertice> addVertice(vertice: V): V {
		graph.add(vertice)
		return vertice
	}

	fun connect(from: Vertice, fromPort: OutputPort<T> = from.getOutput(), to: Vertice, toPort: InputPort<T> = to.getInput()): Net<T> {
		val net = NetImpl<T>()
		graph.add(net)
		net.connect(fromPort)
		net.connect(toPort)
		return net
	}
}