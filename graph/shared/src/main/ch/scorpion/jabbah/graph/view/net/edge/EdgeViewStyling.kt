package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyling
import ch.scorpion.jabbah.graph.view.port.PortView

/** An abstraction being able to draw an [EdgeView] with a particular [NetViewStyle].*/
interface EdgeViewStyling : NetViewStyling {

    /**
     * The width of segments drawn by this [EdgeViewStyling]. Uses e.g. by [PortView]s to adjust
     * the position of external labels.
     */
    val width: Int
}