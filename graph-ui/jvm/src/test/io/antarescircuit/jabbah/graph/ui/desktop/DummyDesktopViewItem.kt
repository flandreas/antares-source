package io.antarescircuit.jabbah.graph.ui.desktop

import dev.mokkery.mock
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import javax.swing.JPanel

internal class DummyDesktopViewItem(
    private val name: String,
    private val item: GraphDesktopViewItem
) : JPanel(), GraphDesktopViewItem by item {

    constructor(name: String) : this(name, mock())

    override val drawingView: DrawingView<GraphElementView<*>, GraphView>? = null

    override fun toString(): String = "Item: $name"
}
