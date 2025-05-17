package ch.scorpion.jabbah.base.invocation

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.Clipboard
import ch.scorpion.jabbah.base.ui.UIBasics
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.output.StringBuilderWriter
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

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
	private val textField = UiUtil.createHtmlEditorPane(
		Translations.getString("base.unexpectedError.text"),
		"base.unexpectedError.title")
	private val ignoreCheckbox = JCheckBox(Translations.getString("base.action.doNotShowAgain.text"))

	init {
		buildUI()
		sendToBackend()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		val iconPanel = JPanel()
		iconPanel.layout = BoxLayout(iconPanel, BoxLayout.PAGE_AXIS)
		iconPanel.add(createErrorIcon())
		iconPanel.add(Box.createVerticalGlue())
		add(iconPanel, BorderLayout.WEST)

		val contentPanel = JPanel()
		contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)

		textField.alignmentX = LEFT_ALIGNMENT

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

	@OptIn(DelicateCoroutinesApi::class)
    private fun sendToBackend() {
		val description = buildClipboardInfo()
		GlobalScope.launch(Dispatchers.IO) {
			BaseModuleJvm.unexpectedErrorService.sendUnexpectedError(description)
		}
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