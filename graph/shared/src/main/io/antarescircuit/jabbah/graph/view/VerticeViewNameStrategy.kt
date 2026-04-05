package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.graph.model.GraphPort
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView

/**
 * Strategy for determining names for [VerticeView] when they are connected to an [EdgeView].
 * Implementations must only produce names that are unique in a [VerticeView].
 */
interface VerticeViewNameStrategy {

    /**
     * Tries to determine a name for a [VerticeView] that has been connected
     * to the specified [EdgeView]
     *
     * @param portOwner the [Vertice] owning all the [Port Ports] among which the name must be unique.
     * Often the same as [vertice], but not always
     * @param vertice the [Vertice] whose name is determined
     * @return the determined name, or `null` if no suitable name could be determined
     */
    fun getConnectedName(portOwner: Vertice, vertice: Vertice, edgeView: EdgeView<*>): String?
}

/**
 * Default [VerticeViewNameStrategy] that uses the following priorities when
 * determining a name:
 *
 * 1. Name of destination [Port] of [EdgeView]
 * 2. Name of origin [Port] of [EdgeView]
 * 3. Name of first suitable input [Port] of [Net]
 * 4. Name of first suitable output [Port] of [Net]
 */
open class VerticeViewNameStrategyImpl : VerticeViewNameStrategy {

    override fun getConnectedName(portOwner: Vertice, vertice: Vertice, edgeView: EdgeView<*>): String? {
        ifFree(portOwner, vertice, destPortNameOfEdgeView(edgeView))?.let { return it }
        ifFree(portOwner, vertice, origPortNameOfEdgeView(edgeView))?.let { return it }

        inputPortOfNet(portOwner, vertice, edgeView)?.let { return it }
        outputPortOfNet(portOwner, vertice, edgeView)?.let { return it }

        return null
    }

    /**
     * Returns the maximum length of the generated name of [Vertice].
     */
    protected open fun getMaxNameLength(vertice: Vertice): Int =
        when (vertice is OscilloscopeProbeVertice<*>) {
            true -> OscilloscopeProbeVerticeView.MAX_PROBE_NAME_LENGTH
            else -> Int.MAX_VALUE
        }

    /**
     * Returns the name to be used when a [Vertice] is connected to [port].
     * This is often the name of the [Port] (if set), but can also be the name of its owner (single-port) [Vertice],
     * where the [Port]'s name is often not set.
     */
    protected open fun portName(port: Port<*>?): String? =
        when (port?.owner) {
            is GraphPort<*> -> port.owner?.name
            else -> port?.name
        }

    private fun ifFree(portOwner: Vertice, vertice: Vertice, name: String?): String? =
        if (isFreeAndShortEnough(portOwner, vertice, name)) name else null

    private fun isFreeAndShortEnough(portOwner: Vertice, vertice: Vertice, name: String?): Boolean =
        name != null && name.length <= getMaxNameLength(vertice) && !portOwner.hasPort(name)

    private fun destPortNameOfEdgeView(edgeView: EdgeView<*>): String? =
        portName(edgeView.destination?.port)

    private fun origPortNameOfEdgeView(edgeView: EdgeView<*>): String? =
        portName(edgeView.origin?.port)

    private fun inputPortOfNet(portOwner: Vertice, vertice: Vertice, edgeView: EdgeView<*>): String? =
        portTypeOfNet(portOwner, vertice, edgeView) { it.portType.isInput }

    private fun outputPortOfNet(portOwner: Vertice, vertice: Vertice, edgeView: EdgeView<*>): String? =
        portTypeOfNet(portOwner, vertice, edgeView) { it.portType.isOutput }

    private fun portTypeOfNet(portOwner: Vertice, vertice: Vertice, edgeView: EdgeView<*>, typeCond: (Port<*>) -> Boolean): String? =
        edgeView.model.ports
            .filter { typeCond.invoke(it) }
            .firstOrNull { isFreeAndShortEnough(portOwner, vertice, portName(it)) }
            ?.name
}