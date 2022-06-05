package ch.scorpion.jabbah.draw.view.find

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.Dimension
import javax.swing.*

class SearchBarSwing(
	private val searchable: Searchable
) : JPanel() {

	private val label = JLabel("Search: ")

	private val textField = JTextField()

	private val closeAction = CloseAction()

	init {
		buildUI()
	}

	private fun buildUI() {
		layout = BoxLayout(this, BoxLayout.LINE_AXIS)

		textField.maximumSize = Dimension(200, textField.preferredSize.height)
		textField.preferredSize = Dimension(200, textField.preferredSize.height)

		add(label)
		add(textField)
		add(Box.createHorizontalGlue())
		add(UiUtil.createToolBarButton(closeAction))
	}

	fun handleShown() {
		SwingUtilities.invokeLater {
			textField.requestFocusInWindow()
		}
	}

	fun handleHidden() {
		textField.text = ""
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