package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.DslParser
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.ParserFactory
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.LineNumberTextArea
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.HelpAction
import ch.scorpion.jabbah.base.ui.UIBasics
import java.awt.*
import javax.swing.*

class ScriptPropertyPanel(
	script: String,
	editable: Boolean = true,
	private val helpId: HelpId? = null,
	private val parserFactory: ParserFactory? = BaseModule.parserFactory,
	private val variables: Iterator<String>? = null,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		private val ERROR_ICON = UiUtil.themedIcon("/img/error-16.png")
		private val CORRECT_ICON = UiUtil.themedIcon("/img/checkmark.png")

		/**
		 * Allows the user to edit a script in a popup dialog.
		 * @param parserFactory creates the [DslParser] used in the "check" function, or `null` if "check" is not supported
		 * @return the edited script, or `null` if the user closed the popup dialog with 'Cancel'.
		 */
		fun showAsDialog(
			parent: Frame = JFrame.getFrames()[0],
			script: String,
			propertyName: String,
			editable: Boolean = true,
			helpId: HelpId? = null,
			variables: Iterator<String>? = null,
			parserFactory: ParserFactory? = BaseModule.parserFactory
		): String? {
			val builder = DialogBuilder<ScriptPropertyPanel>(parent)
				.content { dialog -> ScriptPropertyPanel(script, editable, helpId, parserFactory, variables) { dialog.dispose() } }
				.title(propertyName)
				.preferredSize(Dimension(600, 500))
				.minimumSize(Dimension(300, 200))
				.defaultButton { if (editable) it.okButton else it.closeButton }

			builder.show()

			return builder.content.textToReturn
		}
	}

	private val scriptTextArea = LineNumberTextArea(editable, script)

	private val messageLabel = JLabel(" ", SwingConstants.LEADING)

	/** Display row and column of the caret location. */
	private val statusLabel = JLabel("")

	private var textToReturn: String? = null

	private val okAction = OkAction()
	private val cancelAction = CancelAction()
	private val checkAction = CheckAction() // only used if parserFactory available
	private val closeAction = CloseAction()
	private val okButton = createButton(okAction)
	private val closeButton = createButton(closeAction)

	init {
		buildUI(editable)
		scriptTextArea.mainTextArea.addCaretListener { updateCaretLocation() }
		updateStatus(1, 1)
	}

	private fun buildUI(editable: Boolean) {
		layout = BorderLayout(5, 10)
		border = UIBasics.createDialogBorder()

		add(buildContentPanel(), BorderLayout.CENTER)
		add(buildButtonPanel(editable), BorderLayout.SOUTH)
		variables?.let {
			add(buildDocumentationComponent(it), BorderLayout.EAST)
		}

		SwingUtilities.invokeLater {
			scriptTextArea.mainTextArea.requestFocusInWindow()
		}
	}

	private fun buildContentPanel(): JPanel {
		val panel = JPanel(BorderLayout(0, 5))
		panel.add(scriptTextArea, BorderLayout.CENTER)
		panel.add(buildStatusPanel(), BorderLayout.SOUTH)
		return panel
	}

	private fun buildDocumentationComponent(variables: Iterator<String>): JComponent {
		val builder = StringBuilder("${Translations.getString("edit.property.variables.text")}: <br><br>")
		variables.forEach { builder.append("$it<br>") }
		val textPane = JEditorPane()
		textPane.border = null
		textPane.contentType = "text/html"
		textPane.isEditable = false
		textPane.text = builder.toString()

		val scrollPane = JScrollPane(textPane)
		scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
		scrollPane.border = null

		return scrollPane
	}

	private fun buildStatusPanel(): JPanel {
		messageLabel.alignmentX = Component.LEFT_ALIGNMENT
		messageLabel.border = null

		val panel = JPanel(BorderLayout())
		panel.add(messageLabel, BorderLayout.CENTER)
		panel.add(statusLabel, BorderLayout.EAST)
		return panel;
	}

	private fun buildButtonPanel(editable: Boolean): JPanel {
		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		helpId?.let { buttonPanel.add(UiUtil.createToolBarButton(HelpAction(it))) }
		buttonPanel.add(Box.createHorizontalGlue())
		if (editable) {
			fillEditableButtonPanel(buttonPanel)
		} else {
			buildNonEditableButtonPanel(buttonPanel)
		}
		return buttonPanel
	}

	private fun fillEditableButtonPanel(panel: JPanel) {
		panel.add(Box.createHorizontalGlue())
		if (parserFactory != null) {
			panel.add(createButton(checkAction))
			panel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GROUP_GAP))
		}
		UIBasics.addButtons(panel, okButton, createButton(cancelAction))
	}

	private fun buildNonEditableButtonPanel(panel: JPanel) {
		if (parserFactory != null) {
			panel.add(createButton(checkAction))
			panel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GROUP_GAP))
		}
		panel.add(closeButton)
	}

	private fun createButton(action: Action): JButton = JButton(ActionWrapperSwing(action))

	private fun highlightError(error: DslError) {
		scriptTextArea.mainTextArea.requestFocus()
		scriptTextArea.mainTextArea. select(error.location.pos, error.location.pos + 1)
	}

	private fun updateCaretLocation() {
		try {
			val caretPos = scriptTextArea.mainTextArea.caretPosition
			var line = scriptTextArea.mainTextArea.getLineOfOffset(caretPos)
			var column = caretPos - scriptTextArea.mainTextArea.getLineStartOffset(line) + 1
			line += 1
			updateStatus(line, column)
		} catch (e: Exception) {
			// empty
		}
	}

	private fun updateStatus(line: Int, column: Int) {
		statusLabel.text = "$line:$column"
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
			if (parserFactory == null) {
				return
			}
			try {
				parserFactory.invoke(scriptTextArea.text, null).parse()

				messageLabel.text = Translations.getString("edit.dsl.check.success.msg")
				messageLabel.icon = CORRECT_ICON
			} catch (e: DslError) {
				messageLabel.text = e.toString()
				messageLabel.icon = ERROR_ICON
				highlightError(e)
			}
		}
	}
}