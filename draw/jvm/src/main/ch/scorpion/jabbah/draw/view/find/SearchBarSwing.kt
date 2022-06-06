package ch.scorpion.jabbah.draw.view.find

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.Dimension
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SearchBarSwing(
	private val searchable: Searchable
) : JPanel() {

	private val label = JLabel("Search: ")

	private val searchField = JTextField()

	private val closeAction = CloseAction()

	init {
		buildUI()
		searchField.document.addDocumentListener(object : DocumentListener {
			override fun insertUpdate(e: DocumentEvent?) { search() }
			override fun removeUpdate(e: DocumentEvent?) { search() }
			override fun changedUpdate(e: DocumentEvent?) { search() }
		})
	}

	private fun buildUI() {
		layout = BoxLayout(this, BoxLayout.LINE_AXIS)

		searchField.maximumSize = Dimension(200, searchField.preferredSize.height)
		searchField.preferredSize = Dimension(200, searchField.preferredSize.height)

		add(label)
		add(searchField)
		add(Box.createHorizontalGlue())
		add(UiUtil.createToolBarButton(closeAction))
	}

	fun handleShown() {
		SwingUtilities.invokeLater {
			searchField.requestFocusInWindow()
		}
	}

	fun handleHidden() {
		searchField.text = ""
	}

	private fun search() {
		searchable.execute(SearchRequest(searchField.text))
	}

	private inner class CloseAction : AbstractAction("base.action.close") {

		init {
			imagePath = "/img/close-16.png"
		}

		override fun execute(event: ActionEvent) {
			searchable.hideSearchBar()
		}
	}
}