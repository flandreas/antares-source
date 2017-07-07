package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.graph.view.VerticeView

/**
 * Utility class that supports reuse of [PortView] properties when a [VerticeView] replaces its [PortView]s.
 */
class PortViewReuser(val verticeView: VerticeView<*>) {

    private val map = mutableMapOf<Int, PortView<*>>()

    init {
        for (portView in verticeView.getPortViews()) {
            map.put(portView.port.portId, portView)
        }
    }

    fun reuse() {
        for (portView in verticeView.getPortViews()) {
            val oldPortView = map.get(portView.port.portId)
            if (oldPortView != null) {
                portView.reuseFrom(oldPortView)
            }
        }
    }
}