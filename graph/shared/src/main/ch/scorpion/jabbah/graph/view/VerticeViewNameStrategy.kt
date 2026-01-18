package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView

/**
 * Strategy for determining names for [VerticeView] when they are connected to an [EdgeView].
 * Implementations must only produce names that are unique in a [VerticeView].
 */
interface VerticeViewNameStrategy {

    /**
     * Tries to determine a name for a [VerticeView] that has been connected
     * to the specified [EdgeView]
     *
     * @return the determined name, or `null` if no suitable name could be determined
     */
    fun getConnectedName(vertice: Vertice, edgeView: EdgeView<*>): String?
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

    override fun getConnectedName(vertice: Vertice, edgeView: EdgeView<*>): String? {
        ifFree(vertice, destPortNameOfEdgeView(edgeView))?.let { return it }
        ifFree(vertice, origPortNameOfEdgeView(edgeView))?.let { return it }

        inputPortOfNet(vertice, edgeView)?.let { return it }
        outputPortOfNet(vertice, edgeView)?.let { return it }

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

    private fun ifFree(vertice: Vertice, name: String?): String? =
        if (isFreeAndShortEnough(vertice, name)) name else null

    private fun isFreeAndShortEnough(vertice: Vertice, name: String?): Boolean =
        name != null && name.length <= getMaxNameLength(vertice) && !vertice.hasPort(name)

    private fun destPortNameOfEdgeView(edgeView: EdgeView<*>): String? =
        portName(edgeView.destination?.port)

    private fun origPortNameOfEdgeView(edgeView: EdgeView<*>): String? =
        portName(edgeView.origin?.port)

    private fun inputPortOfNet(vertice: Vertice, edgeView: EdgeView<*>): String? =
        portTypeOfNet(vertice, edgeView) { it.portType.isInput }

    private fun outputPortOfNet(vertice: Vertice, edgeView: EdgeView<*>): String? =
        portTypeOfNet(vertice, edgeView) { it.portType.isOutput }

    private fun portTypeOfNet(vertice: Vertice, edgeView: EdgeView<*>, typeCond: (Port<*>) -> Boolean): String? =
        edgeView.model.ports
            .filter { typeCond.invoke(it) }
            .firstOrNull { isFreeAndShortEnough(vertice, portName(it)) }
            ?.name
}