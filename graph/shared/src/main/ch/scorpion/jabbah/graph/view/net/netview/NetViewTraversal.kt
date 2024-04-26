package ch.scorpion.jabbah.graph.view.net.netview

import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.NetView

/**
 * The cumulative result of traversing a [NetView] in order to collect all its [Port]s.
 * @property [ports] the collected [Port]s
 * @property [edgeViews] the visited [EdgeView]s used to avoid endless recursion
 */
data class NetViewTraversal<T: Any>(
    val ports: MutableSet<Port<T>> = mutableSetOf(),
    val edgeViews: MutableSet<EdgeView<T>> = mutableSetOf()
)