package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.PlaceholderTextField
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/** Contains a [LibraryTreeViewSwing] and a search field for filtering the displayed nodes.*/
class LibraryTreePanel(
	val controller: LibraryTreeViewController
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

		searchField.columns = 30

		val treeViewScrollPane = JScrollPane(
			controller.view as Component,
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

		(controller.view as LibraryTreeViewSwing).model = LibraryTreeModelBuilderSwing(
			controller.library,
			controller.project,
			filter
		).build()
		SwingUtilities.invokeLater {
			JTreeUtil.expandAll(controller.view as LibraryTreeViewSwing)
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