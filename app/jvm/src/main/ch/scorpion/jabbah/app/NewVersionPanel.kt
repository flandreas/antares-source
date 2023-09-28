package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*
import javax.swing.event.HyperlinkEvent

class NewVersionPanel(
	private val newVersion: ApplicationVersion,
	private val service: RemoteControlService = AppModuleJvm.remoteControlService,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		fun showAsDialog(
			parent: JFrame,
			newVersion: ApplicationVersion,
			service: RemoteControlService = AppModuleJvm.remoteControlService
		) {
			DialogBuilder<NewVersionPanel>(parent)
				.title(Translations.getString("application.newVersion.title"))
				.content { dialog -> NewVersionPanel(newVersion, service) { dialog.dispose() } }
				.defaultButton { it.okButton }
				.nonResizable()
				.show()
		}
	}

	private val okAction = OkAction()
	private val okButton = createButton(okAction)
	private val textField = JEditorPane()
	private val ignoreCheckbox = JCheckBox(Translations.getString("application.newVersion.ignore"))

	init {
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		val iconPanel = JPanel()
		iconPanel.layout = BoxLayout(iconPanel, BoxLayout.PAGE_AXIS)
		iconPanel.add(createInfoIcon())
		iconPanel.add(Box.createVerticalGlue())
		add(iconPanel, BorderLayout.WEST)

		textField.isEditable = false
		textField.contentType = "text/html"
		textField.text = Translations.getString("application.newVersion.text", newVersion.toString())
		textField.addHyperlinkListener {
			if (HyperlinkEvent.EventType.ACTIVATED == it.eventType) {
				System.browse(it.url.toString(), Translations.getString("application.newVersion.title"))
			}
		}
		textField.alignmentX = Component.LEFT_ALIGNMENT
		add(textField, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)

		ignoreCheckbox.alignmentX = Component.LEFT_ALIGNMENT
		buttonPanel.add(ignoreCheckbox)
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(okButton)
		buttonPanel.add(Box.createHorizontalStrut(5))
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun createInfoIcon(): JComponent {
		val icon = UIManager.getIcon("OptionPane.informationIcon")
		return JLabel(icon, SwingConstants.LEFT)
	}

	private fun createButton(action: Action): JButton =
		JButton(ActionWrapperSwing(action))

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			if (ignoreCheckbox.isSelected) {
				service.ignoreNewVersion(newVersion)
			}
			closeHandler()
		}
	}
}