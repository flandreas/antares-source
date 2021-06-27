package ch.scorpion.jabbah.graph.view.net.netview

import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.NetViewElement

/**
 * Partitions a [NetView] if it is not contiguous any more after one of its [NetViewElement]s
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
	fun partition(): Set<NetView<Any>> {
		val netViews = mutableSetOf<NetView<Any>>()

		val reachability = calculateReachability()
		if (reachability.size > 1) {
			reachability.forEach {
				netViews.add(netView.splitOff(it))
			}
		}

		return netViews
	}

	private fun calculateReachability(): Set<Set<Port<Any>>> {
		val result = mutableSetOf<MutableSet<Port<Any>>>()
		netView.getElements().forEach { element ->
			val connectedPorts = element.connectedPorts
			if (connectedPorts.isNotEmpty()) {
				val match = result.firstOrNull { it.intersect(connectedPorts).isNotEmpty() }
				if (match != null) {
					match.addAll(connectedPorts)
				} else {
					result.add(connectedPorts.toMutableSet())
				}
			}
		}
		return result
	}
}