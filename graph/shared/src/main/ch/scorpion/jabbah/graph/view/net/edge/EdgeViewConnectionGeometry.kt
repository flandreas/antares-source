package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * Describes the geometry of an [EdgeView] connected to a [PortView].
 * Can be used by [PortView] to fine-tune the position of its external label.
 *
 * Created by [EdgeView] and set in [PortView].
 * In an alternative design, the [PortView] would query the width of a connected [EdgeView] and adjust
 * its external label position accordingly. This would have required to keep a reference to the [EdgeView],
 * which is something we try to avoid.
 *
 * @param distance the distance from the [PortView]'s connection point the [EdgeView]
 * requires for its own purposes, e.g. for drawing an arrow head
 */
data class EdgeViewConnectionGeometry(
	val edgeViewWidth: Int,
	val distance: Int
)