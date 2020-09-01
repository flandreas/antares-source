package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.style.ThemeEvent
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.CurrentProjectEvent
import ch.scorpion.jabbah.graph.project.ProjectHolder
import ch.scorpion.jabbah.graph.project.ProjectModule
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * A combination of a [LibraryTreeView] and a [LibraryPreviewPanel] for the currently open
 * [Library] and [Project] (if any).
 *
 * Posts a [OpenContainerLibraryElementRequest] on [EventBus] when the user double clicks on a [ContainerLibraryElement].
 */
class LibraryPanel(
	application: Application,
    private val eventBus: EventBus,
    libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
    projectHolder: ProjectHolder = ProjectModule.projectHolder
): JPanel() {

    val libraryTreeView = LibraryTreeView(LibraryTreeViewType.Main, application, libraryHolder.library, projectHolder.project, eventBus)
	private val libraryTreePanel = LibraryTreePanel(libraryTreeView)
    val libraryPreviewPanel = LibraryPreviewPanel(eventBus, libraryTreePanel.libraryTreeView)

    init {
        eventBus.register(ThemeEvent::class) { repaint() }
	    eventBus.register(CurrentLibraryEvent::class) { libraryTreeView.library = it.library }
	    eventBus.register(CurrentProjectEvent::class) { libraryTreeView.project = it.project }

	    libraryTreeView.addMouseListener(DoubleClickListener())
	    libraryTreeView.addKeyListener(EnterKey())

		buildUI()
    }

	private fun buildUI() {
		layout = BorderLayout()
		add(libraryPreviewPanel, BorderLayout.NORTH)
		add(libraryTreePanel, BorderLayout.CENTER)
	}

	private inner class DoubleClickListener : MouseAdapter() {
		override fun mousePressed(e: MouseEvent) {
			if (e.clickCount == 2 && libraryTreeView.getSelectedItem() is ContainerLibraryElement) {
				eventBus.post(OpenContainerLibraryElementRequest(libraryTreeView.getSelectedItem() as ContainerLibraryElement))
			}
		}
	}

	private inner class EnterKey : KeyAdapter() {
		override fun keyPressed(e: KeyEvent) {
			if (e.keyCode == KeyEvent.VK_ENTER && libraryTreeView.getSelectedItem() is ContainerLibraryElement) {
				eventBus.post(OpenContainerLibraryElementRequest(libraryTreeView.getSelectedItem() as ContainerLibraryElement))
				SwingUtilities.invokeLater { libraryTreeView.requestFocusInWindow() }
			}
		}
	}
}