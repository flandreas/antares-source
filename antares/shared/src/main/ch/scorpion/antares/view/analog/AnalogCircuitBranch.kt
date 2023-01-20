package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import kotlin.math.abs

/**
 * Contains the IDs of all [EdgeView] that make up a branch (with all [EdgeView]s
 * having the same electrical current). Positive current (from [EdgeView.origin] to
 * [EdgeView.destination] is indicated with a positive ID, negative current with
 * a negative ID.
 */
class AnalogCircuitBranch {

	private val edgeViewIds = mutableListOf<Int>()

	val size: Int get() = edgeViewIds.size

	fun containsId(edgeViewId: Int): Boolean = edgeViewIds.any { abs(it) == edgeViewId }

	fun containsValue(value: Int): Boolean = edgeViewIds.contains(value)

	fun add(connectableView: ConnectableView, edgeView: EdgeView<*>) {
		val sign = when (edgeView.getConnectionEndpointType(connectableView)) {
			EdgeViewEndpointType.ORIGIN -> 1
			EdgeViewEndpointType.DESTINATION -> -1
			null -> throw IllegalArgumentException("Unconnected EdgeView not supported")
		}
		edgeViewIds.add(sign * edgeView.id)
	}
}