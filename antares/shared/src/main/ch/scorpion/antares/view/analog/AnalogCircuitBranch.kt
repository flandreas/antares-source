package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.analog.AnalogVertice
import ch.scorpion.antares.model.analog.AnalogPort
import ch.scorpion.jabbah.base.collection.indexOfFirstOrNull
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import kotlin.math.abs

/**
 * Contains the IDs of all [EdgeView] that make up a branch (with all [EdgeView]s
 * having the same electrical current). Positive current (from [EdgeView.origin] to
 * [EdgeView.destination] is indicated with a positive ID, negative current with
 * a negative ID.
 */
class AnalogCircuitBranch(
	private val fromNode: Boolean
) {

	companion object {

		private fun getBranch(edgeView: EdgeView<*>, branches: List<AnalogCircuitBranch>): AnalogCircuitBranch? =
			branches.firstOrNull { it.containsId(edgeView.id) }

		private fun isPositive(edgeView: EdgeView<*>, branches: List<AnalogCircuitBranch>): Boolean =
			getBranch(edgeView, branches)!!.isPositive(edgeView.id)

		/** Returns the index in [branches] that contains the specified [EdgeView].*/
		fun getBranchId(edgeView: EdgeView<*>, branches: List<AnalogCircuitBranch>): Int? =
			branches.indexOfFirstOrNull { it.containsId(edgeView.id) }

		fun getNodeViewBranchIds(nodeView: NodeView<*>, branches: List<AnalogCircuitBranch>): Set<Int> {
			val result = mutableSetOf<Int>()
			nodeView.getEdgeViews().forEach {
				getBranchId(it, branches)?.let { branchId ->
					result.add(branchId)
				}
			}
			return result
		}

		/**
		 * Returns the current variable index (i.e. the index in [branches] of the [EdgeView]
		 * connected to the [AnalogPort] with [portId].
		 */
		fun getCurrentVariableIndex(
			circuitView: AnalogGraphView,
			vertice: AnalogVertice,
			branches: List<AnalogCircuitBranch>,
			portId: Int = 1
		): Int = getBranchId(
			circuitView.getEdgeView(vertice.getPort<AnalogSignal>(portId))!!,
			branches
		)!!

		fun isCurrentPositive(
			circuitView: AnalogGraphView,
			vertice: AnalogVertice,
			branches: List<AnalogCircuitBranch>,
			portId: Int = 1
		): Boolean = isPositive(
			circuitView.getEdgeView(vertice.getPort<AnalogSignal>(portId))!!,
			branches)
	}

	private val _edgeViewIds = mutableSetOf<Int>()

	val canBeMerged: Boolean get() = !fromNode

	val edgeViewIds: Set<Int> get() = _edgeViewIds

	val size: Int get() = _edgeViewIds.size

	var isOpen: Boolean = false
		private set

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
		isOpen = isOpen || !edgeView.isFullyConnected
		_edgeViewIds.add(sign * edgeView.id)
	}

	/** Merges [branch] into this [AnalogCircuitBranch]. */
	fun merge(branch: AnalogCircuitBranch) {
		check(canBeMerged)
		_edgeViewIds.addAll(branch._edgeViewIds)
	}
}