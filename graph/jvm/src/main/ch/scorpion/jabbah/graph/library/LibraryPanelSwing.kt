package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelController
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelView
import org.jdesktop.swingx.JXTaskPane
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * Displays a [LibraryTreeViewSwing] and a [LibraryPreviewPanel] that shows a preview of the selected
 * [LibraryItem]. The [LibraryPreviewPanel] is collapsible.
 */
class LibraryPanelSwing(
	controller: LibraryPanelController,
	application: Application,
    eventBus: EventBus
): JPanel(), LibraryPanelView {

    val libraryPreviewPanel = LibraryPreviewPanel(eventBus, controller.libraryTreeViewController)

	private val libraryTreePanel: LibraryTreePanelSwing

    private val libraryTreeView = LibraryTreeViewSwing(controller.libraryTreeViewController, application)

	private val taskPane = JXTaskPane(Translations.getString("graph.preview.name"))

    init {
	    controller.view = this
	    libraryTreePanel = LibraryTreePanelSwing(controller.libraryTreePanelController, libraryTreeView)

		buildUI()
    }

	override fun dispose() {
		BaseModule.settings.set("libraryPanel.previewCollapsed", taskPane.isCollapsed)
		libraryPreviewPanel.dispose()
	}

	override fun refresh() {
		invalidate()
		validate()
	}

	private fun buildUI() {
		layout = BorderLayout(0, 8)

		taskPane.isCollapsed = BaseModule.settings.getBoolean("libraryPanel.previewCollapsed", false)
		taskPane.isAnimated = false
		taskPane.add(libraryPreviewPanel)

		add(taskPane, BorderLayout.NORTH)
		add(libraryTreePanel, BorderLayout.CENTER)
	}
}