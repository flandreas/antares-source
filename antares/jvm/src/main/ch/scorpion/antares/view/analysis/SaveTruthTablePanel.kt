package ch.scorpion.antares.view.analysis

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Frame
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SaveTruthTablePanel(
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		fun showAsDialog(title: String, parent: Frame): String? {
			val builder = DialogBuilder<SaveTruthTablePanel>(parent)
				.content { dialog -> SaveTruthTablePanel { dialog.dispose() } }
				.title(title)
				.defaultButton { it.okButton }
				.nonResizable()
				.show()

			return builder.content.result
		}
	}

	private val okAction = OkAction()
	private val okButton = createButton(okAction)
	private val cancelAction = CancelAction()

	private val nameLabel = JLabel(Translations.getString("library.newTruthTable.name"))
	private val nameField = JTextField(20)

	private var result: String? = null

	init {
		buildUI()
		updateOkAction()

		nameField.document.addDocumentListener(object : DocumentListener {
			override fun insertUpdate(e: DocumentEvent?) { updateOkAction() }
			override fun removeUpdate(e: DocumentEvent?) { updateOkAction() }
			override fun changedUpdate(e: DocumentEvent?) { updateOkAction() }
		})
	}

	private fun updateOkAction() {
		okAction.enabled = StringUtils.isNotBlank(nameField.text)
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		val contentPanel = JPanel()
		contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)

		nameLabel.horizontalAlignment = SwingConstants.RIGHT
		nameLabel.alignmentX = Component.LEFT_ALIGNMENT
		nameField.maximumSize = nameField.preferredSize
		nameField.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(nameLabel)
		contentPanel.add(Box.createVerticalStrut(2))
		contentPanel.add(nameField)
		contentPanel.add(Box.createVerticalStrut(12))

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(buttonPanel, okButton, createButton(cancelAction))

		add(contentPanel, BorderLayout.CENTER)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun createButton(action: Action): JButton = JButton(ActionWrapperSwing(action))

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			result = nameField.text
			closeHandler()
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			result = null
			closeHandler()
		}
	}
}