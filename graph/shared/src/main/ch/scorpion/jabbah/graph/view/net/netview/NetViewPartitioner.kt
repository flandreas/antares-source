package ch.scorpion.jabbah.graph.view.net.netview

import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.NetViewElement

/**
 * Partitions a [NetView] if it is not contiguous anymore after one of its [NetViewElement]s
 * has been removed.
 *
 * @property netView the [NetView] from which a [NetViewElement] has been removed
 */
class NetViewPartitioner(val netView: NetView<Any>) {

	/**
	 * Performs the partitioning of [netView] (if necessary) and returns the newly
	 * created part [NetView]s. Does also all the necessary partitioning of the
	 * underlying [Net] and reconnecting of references to these [Net]s
	 *
	 * @return the newly created partitioned [NetView], or an empty [Set]
	 * if no partitioning was necessary
	 */
	fun partition(): Set<NetView<*>> {
		val netViews = mutableSetOf<NetView<*>>()

		val reachability = calculateReachability()
		if (reachability.size > 1) {
			reachability.forEach {
				// The newly created Graph has to be added to the Graph by the caller of this method
				netViews.add(netView.splitOff(it))
			}
		}

		return netViews
	}

	private fun calculateReachability(): Set<Set<Port<Any>>> {
		val result = mutableSetOf<MutableSet<Port<Any>>>()

		netView
			.getElements()
			.filterIsInstance<EdgeView<Any>>()
			.forEach { element ->
				val traversal = NetViewTraversal<Any>()
				element.traverse(traversal)
				val match = result.firstOrNull { it.intersect(traversal.ports).isNotEmpty() }
				if (match != null) {
					match.addAll(traversal.ports)
				} else {
					result.add(traversal.ports)
				}
		}

		return result
	}
}