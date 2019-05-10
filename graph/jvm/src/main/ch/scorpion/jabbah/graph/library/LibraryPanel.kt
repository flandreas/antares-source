package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.style.ThemeEvent
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.CurrentProjectEvent
import ch.scorpion.jabbah.graph.project.ProjectHolder
import ch.scorpion.jabbah.graph.project.ProjectModule
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.JScrollPane

/**
 * A combination of a [LibraryTreeView] and a [LibraryPreviewPanel] for the currently open
 * [Library] and [Project] (if any).
 *
 * Posts a [OpenContainerLibraryElementRequest] on [EventBus] when the user double clicks on a [ContainerLibraryElement].
 */
class LibraryPanel(
    private val eventBus: EventBus,
    libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
    projectHolder: ProjectHolder = ProjectModule.projectHolder
): JPanel() {

    private val libraryTreeView = LibraryTreeView(libraryHolder.library, projectHolder.project, eventBus)
    val libraryPreviewPanel = LibraryPreviewPanel(eventBus, libraryTreeView)

    init {
        eventBus.register(ThemeEvent::class) { repaint() }
	    eventBus.register(CurrentLibraryEvent::class) { libraryTreeView.library = it.library }
	    eventBus.register(CurrentProjectEvent::class) { libraryTreeView.project = it.project }

	    libraryTreeView.addMouseListener(DoubleClickListener())

		buildUI()
    }

	private fun buildUI() {
		layout = BorderLayout()
		val treeViewScrollPane = JScrollPane(
			libraryTreeView,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

		add(libraryPreviewPanel, BorderLayout.NORTH)
		add(treeViewScrollPane, BorderLayout.CENTER)
	}

	private inner class DoubleClickListener : MouseAdapter() {
		override fun mousePressed(e: MouseEvent) {
			if (e.clickCount == 2 && libraryTreeView.getSelectedItem() is ContainerLibraryElement) {
				eventBus.post(OpenContainerLibraryElementRequest(libraryTreeView.getSelectedItem() as ContainerLibraryElement))
			}
		}
	}
}