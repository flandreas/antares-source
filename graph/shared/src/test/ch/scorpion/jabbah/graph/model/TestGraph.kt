package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.graph.model.net.NetImpl

/**
 * Contains a [GraphImpl] consisting of two [TestVertice]s that are connected by a [NetImpl]
 * for signals of type [Boolean].
 */
open class TestGraph(eventBus: EventBus) {

    val graph = GraphImpl(eventBus = eventBus)
    val v1: TestVertice
    val v2: TestVertice
    val net: Net<Boolean>

    init {
        net = NetImpl<Boolean>()
        v1 = TestVertice()
        v2 = TestVertice()
        net.connect(v1.getOutput())
        net.connect(v2.getInput())
        graph.add(v1).add(v2).add(net)
    }
}