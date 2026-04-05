package io.antarescircuit.jabbah.base.invocation

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.Clipboard
import io.antarescircuit.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
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
			Clipboard.setStringContents(BaseModuleJvm.unexpectedErrorService.buildDescription(versionId, stackTrace))
		}
	}
}