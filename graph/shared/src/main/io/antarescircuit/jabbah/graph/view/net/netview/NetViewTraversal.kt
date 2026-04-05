package io.antarescircuit.jabbah.graph.view.net.netview

import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.NetView

/**
 * The cumulative result of traversing a [NetView] in order to collect all its [Port]s.
 * @property [ports] the collected [Port]s
 * @property [edgeViews] the visited [EdgeView]s used to avoid endless recursion
 */
data class NetViewTraversal<T: Any>(
    val ports: MutableSet<Port<T>> = mutableSetOf(),
    val edgeViews: MutableSet<EdgeView<T>> = mutableSetOf()
)