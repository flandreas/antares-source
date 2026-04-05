package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewStyle
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewStyling
import io.antarescircuit.jabbah.graph.view.port.PortView

/** An abstraction being able to draw an [EdgeView] with a particular [NetViewStyle].*/
interface EdgeViewStyling : NetViewStyling {

    /**
     * The width of segments drawn by this [EdgeViewStyling]. Uses e.g. by [PortView]s to adjust
     * the position of external labels.
     */
    val width: Int
}