package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.style.ThemeEvent
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane

/**
 * A combination of a [LibraryTreeView] and a [LibraryPreviewPanel].
 */
class LibraryPanel(
    eventBus: EventBus,
    libraryHolder: LibraryHolder
): JPanel() {

    val libraryTreeView = LibraryTreeView(eventBus, libraryHolder)
    val libraryPreviewPanel = LibraryPreviewPanel(eventBus, libraryTreeView)

    init {
        eventBus.register(ThemeEvent::class, { repaint() })

        layout = BorderLayout()
        val treeViewScrollPane = JScrollPane(
                libraryTreeView,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

        add(libraryPreviewPanel, BorderLayout.NORTH)
        add(treeViewScrollPane, BorderLayout.CENTER)
    }
}