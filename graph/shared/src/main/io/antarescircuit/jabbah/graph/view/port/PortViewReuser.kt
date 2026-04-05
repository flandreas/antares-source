package io.antarescircuit.jabbah.graph.view.port

import io.antarescircuit.jabbah.graph.view.VerticeView

/**
 * Utility class that supports reuse of [PortView] properties when a [VerticeView] replaces its [PortView]s.
 */
class PortViewReuser(val verticeView: VerticeView<*>) {

    private val map = mutableMapOf<Int, PortView<*>>()

    init {
        for (portView in verticeView.getPortViews()) {
	        map[portView.port.portId] = portView
        }
    }

    fun reuse() {
        for (portView in verticeView.getPortViews()) {
	        map[portView.port.portId]?.let { portView.reuseFrom(it) }
        }
    }
}