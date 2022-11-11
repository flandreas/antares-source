package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelController
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelView
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * Displays a [LibraryTreeViewSwing] and a [LibraryPreviewPanel] that shows a preview of the selected
 * [LibraryItem].
 */
class LibraryPanelSwing(
	controller: LibraryPanelController,
	application: Application,
    eventBus: EventBus
): JPanel(), LibraryPanelView {

    val libraryPreviewPanel = LibraryPreviewPanel(eventBus, controller.libraryTreeViewController)

	private val libraryTreePanel: LibraryTreePanel
    private val libraryTreeView = LibraryTreeViewSwing(controller.libraryTreeViewController, application)

    init {
	    controller.view = this
	    libraryTreePanel = LibraryTreePanel(controller.libraryTreeViewController, libraryTreeView)

		buildUI()
    }

	override fun dispose() { }

	override fun refresh() {
		invalidate()
		validate()
	}

	private fun buildUI() {
		layout = BorderLayout(0, 8)
		add(libraryPreviewPanel, BorderLayout.NORTH)
		add(libraryTreePanel, BorderLayout.CENTER)
	}
}