package ch.scorpion.jabbah.graph.ui.desktop

import dev.mokkery.mock
import javax.swing.JPanel

internal class DummyDesktopViewItem(
    private val name: String,
    private val item: GraphDesktopViewItem
) : JPanel(), GraphDesktopViewItem by item {

    constructor(name: String) : this(name, mock())

    override fun toString(): String = "Item: $name"
}