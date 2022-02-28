package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelController
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelView
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class LibraryPanelSwing(
	private val controller: LibraryPanelController,
	application: Application,
    private val eventBus: EventBus
): JPanel(), LibraryPanelView {

	private val libraryTreePanel: LibraryTreePanel
    val libraryTreeView = LibraryTreeViewSwing(controller.libraryTreeViewController, application)
    val libraryPreviewPanel = LibraryPreviewPanel(eventBus, controller.libraryTreeViewController)

	private val doubleClickListener = DoubleClickListener()
	private val enterKeyListener = EnterKeyListener()

    init {
	    controller.view = this

	    libraryTreePanel = LibraryTreePanel(controller.libraryTreeViewController)

	    libraryTreeView.addMouseListener(doubleClickListener)
	    libraryTreeView.addKeyListener(enterKeyListener)

		buildUI()
    }

	override fun dispose() {
		libraryTreeView.removeMouseListener(doubleClickListener)
		libraryTreeView.removeKeyListener(enterKeyListener)
	}

	override fun refresh() {
		invalidate()
		validate()
	}

	private fun buildUI() {
		layout = BorderLayout(0, 8)
		add(libraryPreviewPanel, BorderLayout.NORTH)
		add(libraryTreePanel, BorderLayout.CENTER)
	}

	private val selectedTreeItem: LibraryItem? get() = controller.libraryTreeViewController.selectedItem

	private inner class DoubleClickListener : MouseAdapter() {
		override fun mousePressed(e: MouseEvent) {
			if (e.clickCount == 2 && selectedTreeItem is LibraryItem) {
				(selectedTreeItem as LibraryItem).open(eventBus)
			}
		}
	}

	private inner class EnterKeyListener : KeyAdapter() {
		override fun keyPressed(e: KeyEvent) {
			if (e.keyCode == KeyEvent.VK_ENTER && selectedTreeItem is LibraryItem) {
				(selectedTreeItem as LibraryItem).open(eventBus)
				SwingUtilities.invokeLater { libraryTreeView.requestFocusInWindow() }
			}
		}
	}
}