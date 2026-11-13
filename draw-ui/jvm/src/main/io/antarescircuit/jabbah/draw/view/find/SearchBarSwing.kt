package io.antarescircuit.jabbah.draw.view.find

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.swing.PlaceholderTextField
import io.antarescircuit.jabbah.base.swing.UiUtil
import java.awt.Dimension
import java.awt.event.ActionEvent as SwingActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
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
		private val DEFAULT_MATCH = SearchMatch.Contains
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
	private val resultLabel = JLabel()
	private val previousButton = JButton("↑")
	private val nextButton = JButton("↓")

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

		searchField.addActionListener { navigate(true) }
		searchField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "previousMatch")
		searchField.actionMap.put("previousMatch", object : javax.swing.AbstractAction() {
			override fun actionPerformed(e: SwingActionEvent?) = navigate(false)
		})
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeSearch")
		actionMap.put("closeSearch", object : javax.swing.AbstractAction() {
			override fun actionPerformed(e: SwingActionEvent?) = searchable.hideSearchBar()
		})
		previousButton.toolTipText = Translations.getString("draw.search.previous")
		previousButton.accessibleContext.accessibleName = previousButton.toolTipText
		previousButton.addActionListener { navigate(false) }
		nextButton.toolTipText = Translations.getString("draw.search.next")
		nextButton.accessibleContext.accessibleName = nextButton.toolTipText
		nextButton.addActionListener { navigate(true) }
		buildUI()
		updateResult()
	}

	private fun buildUI() {
		layout = BoxLayout(this, BoxLayout.LINE_AXIS)

		searchField.maximumSize = Dimension(SEARCH_FIELD_SIZE, searchField.preferredSize.height)
		searchField.preferredSize = Dimension(SEARCH_FIELD_SIZE, searchField.preferredSize.height)
		add(searchField)
		add(Box.createHorizontalStrut(LABEL_DIST))
		add(previousButton)
		add(Box.createHorizontalStrut(2))
		add(nextButton)
		add(Box.createHorizontalStrut(LABEL_DIST))
		add(resultLabel)
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
			searchField.selectAll()
			if (searchField.text.isNotBlank()) search() else updateResult()
		}
	}

	fun handleHidden() { }

	/** Reapply a visible query after the drawing is replaced, without changing keyboard focus. */
	fun refreshSearch() {
		if (searchField.text.isNotBlank()) search() else updateResult()
	}

	private fun search() {
		searchable.execute(createRequest())
		updateResult()
	}

	private fun navigate(forward: Boolean) {
		searchable.navigateSearch(forward)
		updateResult()
	}

	private fun updateResult() {
		val result = searchable.searchResult
		previousButton.isVisible = result != null
		nextButton.isVisible = result != null
		previousButton.isEnabled = (result?.count ?: 0) > 0
		nextButton.isEnabled = previousButton.isEnabled
		resultLabel.text = when {
			result == null -> ""
			result.current > 0 -> Translations.getString("draw.search.currentMatch", result.current, result.count)
			else -> Translations.getString("draw.search.matches", result.count)
		}
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