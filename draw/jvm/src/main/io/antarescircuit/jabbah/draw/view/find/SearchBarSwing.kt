package io.antarescircuit.jabbah.draw.view.find

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.swing.PlaceholderTextField
import io.antarescircuit.jabbah.base.swing.UiUtil
import java.awt.Dimension
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SearchBarSwing(
	private val searchable: Searchable
) : JPanel() {

	companion object {
		private const val SEARCH_FIELD_SIZE = 150
		private const val LABEL_DIST = 5
		private const val FIELD_DIST = 20
		private val DEFAULT_MATCH = SearchMatch.EntireWord
		private const val DEFAULT_IGNORE_CASE = true
	}

	private val searchField = PlaceholderTextField(
		placeholder = Translations.getString("draw.search.text"),
		columns = 20,
		showClearButton = true
	)

	private val matchLabel = JLabel("${Translations.getString("draw.search.match")}:")
	private val matchField = JComboBox<SearchMatch>()

	private val ignoreCaseField = JCheckBox(Translations.getString("draw.search.ignoreCase"))

	private val closeAction = CloseAction()

	init {
		searchField.document.addDocumentListener(object : DocumentListener {
			override fun insertUpdate(e: DocumentEvent?) { search() }
			override fun removeUpdate(e: DocumentEvent?) { search() }
			override fun changedUpdate(e: DocumentEvent?) { search() }
		})

		SearchMatch.values().forEach {
			matchField.addItem(it)
		}
		matchField.selectedItem = DEFAULT_MATCH
		matchField.addActionListener { search() }

		ignoreCaseField.isSelected = DEFAULT_IGNORE_CASE
		ignoreCaseField.addActionListener { search() }

		buildUI()
	}

	private fun buildUI() {
		layout = BoxLayout(this, BoxLayout.LINE_AXIS)

		searchField.maximumSize = Dimension(SEARCH_FIELD_SIZE, searchField.preferredSize.height)
		searchField.preferredSize = Dimension(SEARCH_FIELD_SIZE, searchField.preferredSize.height)
		add(searchField)
		add(Box.createHorizontalStrut(FIELD_DIST))

		matchField.maximumSize = matchField.preferredSize
		add(matchLabel)
		add(Box.createHorizontalStrut(LABEL_DIST))
		add(matchField)
		add(Box.createHorizontalStrut(FIELD_DIST))

		add(ignoreCaseField)

		add(Box.createHorizontalGlue())
		add(UiUtil.createToolBarButton(closeAction))
	}

	fun handleShown() {
		SwingUtilities.invokeLater {
			searchField.requestFocusInWindow()
		}
	}

	fun handleHidden() { }

	private fun search() {
		searchable.execute(createRequest())
	}

	private fun createRequest(): SearchRequest =
		SearchRequest(
			searchField.text,
			matchField.selectedItem as SearchMatch,
			ignoreCaseField.isSelected)

	private inner class CloseAction : AbstractAction("base.action.close") {

		init {
			imagePath = "/img/close-16.png"
		}

		override fun execute(event: ActionEvent) {
			searchable.hideSearchBar()
		}
	}
}