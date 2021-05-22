package ch.scorpion.jabbah.base.invocation

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.ui.Clipboard
import org.apache.commons.io.output.StringBuilderWriter
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*
import javax.swing.event.HyperlinkEvent

class UnexpectedErrorPanel(
	private val versionId: String,
	private val stackTrace: String,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		fun showAsDialog(parent: JFrame, versionId: String, stackTrace: String) {
			DialogBuilder<UnexpectedErrorPanel>(parent)
				.title(Translations.getString("base.unexpectedError.title"))
				.content { dialog -> UnexpectedErrorPanel(versionId, stackTrace, closeHandler = { dialog.dispose() }) }
				.defaultButton { it.okButton }
				.preferredSize(Dimension(350, 220))
				.nonResizable()
				.show()
		}
	}

	private val okAction = OkAction()
	val okButton = createButton(okAction)
	private val copyToClipboardAction = CopyToClipboardAction()
	private val textField = JEditorPane()
	private val ignoreCheckbox = JCheckBox(Translations.getString("base.action.doNotShowAgain.text"))

	init {
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		val iconPanel = JPanel()
		iconPanel.layout = BoxLayout(iconPanel, BoxLayout.PAGE_AXIS)
		iconPanel.add(createErrorIcon())
		iconPanel.add(Box.createVerticalGlue())
		add(iconPanel, BorderLayout.WEST)

		val contentPanel = JPanel()
		contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)

		textField.isEditable = false
		textField.contentType = "text/html"
		textField.text = Translations.getString("base.unexpectedError.text")
		textField.addHyperlinkListener {
			if (HyperlinkEvent.EventType.ACTIVATED == it.eventType) {
				System.browse(it.url.toString(), Translations.getString("base.unexpectedError.title"))
			}
		}
		textField.alignmentX = Component.LEFT_ALIGNMENT

		contentPanel.add(textField)
		contentPanel.add(Box.createVerticalStrut(10))
		contentPanel.add(ignoreCheckbox)
		contentPanel.add(Box.createVerticalStrut(10))

		add(contentPanel, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)

		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(createButton(copyToClipboardAction))
		buttonPanel.add(Box.createHorizontalStrut(5))
		buttonPanel.add(okButton)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun createErrorIcon(): JComponent {
		val icon = UIManager.getIcon("OptionPane.errorIcon")
		return JLabel(icon, SwingConstants.LEFT)
	}

	private fun createButton(action: Action): JButton =
		JButton(ActionWrapperSwing(action))

	private fun buildClipboardInfo(): String {
		val writer = StringBuilderWriter()
		if (versionId.isNotBlank()) {
			writer.appendLine("Version: $versionId")
		}
		writer.append(UserActionTrail.toString())
		writer.append(stackTrace)
		return writer.toString()
	}

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			if (ignoreCheckbox.isSelected) {
				BaseModule.properties.customize(InteractiveErrorHandler.PROP_SHOW_UNEXPECTED_ERROR, false)
			}
			closeHandler()
		}
	}

	private inner class CopyToClipboardAction : AbstractAction("base.action.copyToClipboard") {
		override fun execute(event: ActionEvent) {
			Clipboard.setStringContents(buildClipboardInfo())
		}
	}
}