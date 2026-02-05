package ch.scorpion.jabbah.graph.ui.desktop

data class DockingStartedEvent(
    val graphDesktopViewItem: GraphDesktopViewItem
)

data class DockingFinishedEvent(
    val graphDesktopViewItem: GraphDesktopViewItem
)