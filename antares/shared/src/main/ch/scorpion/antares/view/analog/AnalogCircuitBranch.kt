package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.base.collection.indexOfFirstOrNull
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

	companion object {

		fun getBranch(edgeView: EdgeView<*>, branches: List<AnalogCircuitBranch>): AnalogCircuitBranch? =
			branches.firstOrNull { it.containsId(edgeView.id) }

		fun isPositive(edgeView: EdgeView<*>, branches: List<AnalogCircuitBranch>): Boolean =
			getBranch(edgeView, branches)!!.isPositive(edgeView.id)

		fun getBranchId(edgeView: EdgeView<*>, branches: List<AnalogCircuitBranch>): Int? =
			branches.indexOfFirstOrNull { it.containsId(edgeView.id) }
	}

	private val _edgeViewIds = mutableSetOf<Int>()

	val edgeViewIds: Set<Int> get() = _edgeViewIds

	val size: Int get() = _edgeViewIds.size

	fun containsId(edgeViewId: Int): Boolean = _edgeViewIds.any { abs(it) == edgeViewId }

	fun containsValue(value: Int): Boolean = _edgeViewIds.contains(value)

	fun isPositive(edgeViewId: Int): Boolean = _edgeViewIds.first { abs(it) == edgeViewId } > 0

	fun add(connectableView: ConnectableView, edgeView: EdgeView<*>) {
		if (containsId(edgeView.id)) {
			throw IllegalArgumentException("EdgeView ${edgeView.id} already contained in branch")
		}
		val sign = when (edgeView.getConnectionEndpointType(connectableView)) {
			EdgeViewEndpointType.ORIGIN -> 1
			EdgeViewEndpointType.DESTINATION -> -1
			null -> throw IllegalArgumentException("Unconnected EdgeView not supported")
		}
		_edgeViewIds.add(sign * edgeView.id)
	}

	/** Merges [branch] into this [AnalogCircuitBranch]. */
	fun merge(branch: AnalogCircuitBranch) {
		_edgeViewIds.addAll(branch._edgeViewIds)
	}
}