package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.PlaceholderTextField
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.graph.ui.library.LibraryTreePanel
import ch.scorpion.jabbah.graph.ui.library.LibraryTreePanelController
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/** Contains a [LibraryTreeViewSwing] and a search field for filtering the displayed nodes.*/
class LibraryTreePanelSwing(
	val controller: LibraryTreePanelController,
	private val libraryTreeView: LibraryTreeViewSwing
) : JPanel(), LibraryTreePanel {

	private val searchField = PlaceholderTextField(
		placeholder = Translations.getString("base.action.search.name"),
		showClearButton = true)

	init {
		controller.view = this
		buildUI()
		searchField.document.addDocumentListener(SearchFieldListener())
	}

	override fun dispose() { }

	private fun buildUI() {
		layout = BorderLayout()

		val treeViewScrollPane = JScrollPane(
			libraryTreeView,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

		add(searchField, BorderLayout.NORTH)
		add(treeViewScrollPane, BorderLayout.CENTER)
	}

	override fun filter(filter: LibraryFilter?) {
		libraryTreeView.model = LibraryTreeModelBuilderSwing(
			controller.libraryTreeViewController.library,
			filter
		).build(Graphics2DJvm.fromAwtFont(font))
		SwingUtilities.invokeLater {
			if (filter == null) {
				JTreeUtil.collapseAll(libraryTreeView)
				libraryTreeView.expandToCurrentSavable()
			} else {
				JTreeUtil.expandAll(libraryTreeView)
			}
		}
	}

	override fun clearFilter() {
		searchField.text = ""
	}

	private inner class SearchFieldListener : DocumentListener {

		override fun changedUpdate(e: DocumentEvent?) {
			search()
		}

		override fun insertUpdate(e: DocumentEvent?) {
			search()
		}

		override fun removeUpdate(e: DocumentEvent?) {
			search()
		}

		private fun search() {
			controller.search(searchField.text)
		}
	}
}