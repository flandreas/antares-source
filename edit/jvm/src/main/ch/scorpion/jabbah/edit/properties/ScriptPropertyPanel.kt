package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.ParserFactory
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.LineNumberTextArea
import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.*
import javax.swing.*

class ScriptPropertyPanel(
	script: String,
	editable: Boolean = true,
	private val parserFactory: ParserFactory = BaseModule.parserFactory,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		private val FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)

		private val ERROR_ICON = UiUtil.themedIcon("/img/error-16.png")
		private val CORRECT_ICON = UiUtil.themedIcon("/img/checkmark.png")

		/**
		 * Allows the user to edit a script in a popup dialog.
		 * @return the edited script, or `null` if the user closed the popup dialog with 'Cancel'.
		 */
		fun showAsDialog(
			parent: Frame = JFrame.getFrames()[0],
			script: String,
			propertyName: String,
			editable: Boolean = true,
			parserFactory: ParserFactory = BaseModule.parserFactory
		): String? {
			val builder = DialogBuilder<ScriptPropertyPanel>(parent)
				.content { dialog -> ScriptPropertyPanel(script, editable, parserFactory) { dialog.dispose() } }
				.title(propertyName)
				.preferredSize(Dimension(600, 500))
				.defaultButton { if (editable) it.okButton else it.closeButton }

			builder.show()

			return builder.content.textToReturn
		}
	}

	private val scriptTextArea = LineNumberTextArea(editable, script, FONT)
	private val messageTextField = JLabel("", SwingConstants.LEADING)

	private var textToReturn: String? = null

	private val okAction = OkAction()
	private val cancelAction = CancelAction()
	private val checkAction = CheckAction()
	private val closeAction = CloseAction()
	private val okButton = createButton(okAction)
	private val closeButton = createButton(closeAction)

	init {
		messageTextField.background = Color.yellow

		buildUI(editable)
	}

	private fun buildUI(editable: Boolean) {
		layout = BorderLayout(0, 10)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		val textsPanel = JPanel()
		textsPanel.layout = BoxLayout(textsPanel, BoxLayout.PAGE_AXIS)

		scriptTextArea.alignmentX = Component.LEFT_ALIGNMENT
		textsPanel.add(scriptTextArea)

		textsPanel.add(Box.createVerticalStrut(8))

		messageTextField.alignmentX = Component.LEFT_ALIGNMENT
		messageTextField.border = null
		textsPanel.add(messageTextField)

		add(textsPanel, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		if (editable) {
			fillEditableButtonPanel(buttonPanel)
		} else {
			buildNonEditableButtonPanel(buttonPanel)
		}
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun fillEditableButtonPanel(panel: JPanel) {
		panel.add(Box.createHorizontalGlue())
		panel.add(createButton(checkAction))
		panel.add(Box.createHorizontalStrut(9))
		panel.add(createButton(cancelAction))
		panel.add(Box.createHorizontalStrut(2))
		panel.add(okButton)
	}

	private fun buildNonEditableButtonPanel(panel: JPanel) {
		panel.add(createButton(checkAction))
		panel.add(Box.createHorizontalStrut(9))
		panel.add(closeButton)
	}

	private fun createButton(action: Action): JButton = JButton(ActionWrapperSwing(action))

	private fun highlightError(error: DslError) {
		scriptTextArea.mainTextArea.requestFocus()
		scriptTextArea.mainTextArea. select(error.location.pos, error.location.pos + 1)
	}

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
			textToReturn = scriptTextArea.text
			closeHandler()
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
			textToReturn = null
			closeHandler()
		}
	}

	private inner class CloseAction : AbstractAction("base.action.close") {
		override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
			textToReturn = null
			closeHandler()
		}
	}

	private inner class CheckAction : AbstractAction("edit.dsl.check.action") {
		override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
			try {
				parserFactory.create(scriptTextArea.text, null).parse()

				messageTextField.text = Translations.getString("edit.dsl.check.success.msg")
				messageTextField.icon = CORRECT_ICON
			} catch (e: DslError) {
				messageTextField.text = e.message
				messageTextField.icon = ERROR_ICON
				highlightError(e)
			}
		}
	}
}