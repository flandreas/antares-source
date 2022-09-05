package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.PlaceholderTextField
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/** Contains a [LibraryTreeViewSwing] and a search field for filtering the displayed nodes.*/
class LibraryTreePanel(
	val controller: LibraryTreeViewController,
	private val libraryTreeView: LibraryTreeViewSwing
) : JPanel() {

	private val searchField = PlaceholderTextField(
		placeholder = Translations.getString("base.action.search.name"),
		showClearButton = true)

	init {
		buildUI()
		searchField.document.addDocumentListener(SearchFieldListener())
	}

	private fun buildUI() {
		layout = BorderLayout()

		val treeViewScrollPane = JScrollPane(
			libraryTreeView,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

		add(searchField, BorderLayout.NORTH)
		add(treeViewScrollPane, BorderLayout.CENTER)
	}

	private fun search() {
		val filter: LibraryFilter? = if (StringUtils.isBlank(searchField.text)) {
			null
		} else {
			item -> item.toString().contains(searchField.text, true)
		}

		libraryTreeView.model = LibraryTreeModelBuilderSwing(
			controller.library,
			filter
		).build()
		SwingUtilities.invokeLater {
			if (filter == null) {
				JTreeUtil.collapseAll(libraryTreeView)
				libraryTreeView.expandToCurrentSavable()
			} else {
				JTreeUtil.expandAll(libraryTreeView)
			}
		}
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
	}
}