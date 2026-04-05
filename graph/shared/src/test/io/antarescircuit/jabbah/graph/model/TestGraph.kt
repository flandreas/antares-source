package io.antarescircuit.jabbah.graph.model

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.graph.model.graph.GraphImpl
import io.antarescircuit.jabbah.graph.model.net.NetImpl
import io.antarescircuit.jabbah.graph.view.NetView
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewImpl

/**
 * Contains a [GraphImpl] consisting of two [TestVertice]s that are connected by a [NetImpl]
 * for signals of type [Boolean].
 */
open class TestGraph(eventBus: EventBus) {

    val graph = GraphImpl(eventBus = eventBus)
    val v1: TestVertice
    val v2: TestVertice
    val net: Net<Boolean>
    val netView: NetView<Boolean>

    init {
        net = NetImpl()
	    netView = NetViewImpl(net)
        v1 = TestVertice()
        v2 = TestVertice()
        net.connect(v1.getOutput())
        net.connect(v2.getInput())
        graph.add(v1).add(v2).add(net)
    }
}