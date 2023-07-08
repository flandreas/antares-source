package ch.scorpion.jabbah.app.dump

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.SystemMalfunctionEvent
import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import java.io.File
import java.nio.file.Path
import javax.swing.*
import javax.swing.event.HyperlinkEvent

/** Displays a dialog upon receiving a [SystemMalfunctionEvent].*/
class SystemMalfunctionPanel(
	private val application: DesktopApplication,
	private val event: SystemMalfunctionEvent,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private val LOG by logger(SystemMalfunctionPanel::class)

		fun showAsDialog(application: DesktopApplication, event: SystemMalfunctionEvent, parent: Frame) {
			DialogBuilder<SystemMalfunctionPanel>(parent)
				.title(Translations.getString("application.systemMalfunction.title"))
				.content { dialog -> SystemMalfunctionPanel(application, event) { dialog.dispose() } }
				.defaultButton { it.closeButton }
				.preferredSize(Dimension(400, 280))
				.nonResizable()
				.show()
		}
	}

	private val closeAction = CloseAction()
	private val exportAction = ExportAction()
	private val closeButton = JButton(ActionWrapperSwing(closeAction))
	private val textField = JEditorPane()

	init {
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 20)
		border = UIBasics.createDialogBorder()

		val iconPanel = JPanel()
		iconPanel.layout = BoxLayout(iconPanel, BoxLayout.PAGE_AXIS)
		iconPanel.add(createErrorIcon())
		iconPanel.add(Box.createVerticalGlue())
		add(iconPanel, BorderLayout.WEST)

		val contentPanel = JPanel()
		contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)

		textField.isEditable = false
		textField.contentType = "text/html"
		textField.text = Translations.getString("application.systemMalfunction.text", application.displayName)
		textField.addHyperlinkListener {
			if (HyperlinkEvent.EventType.ACTIVATED == it.eventType) {
				System.browse(it.url.toString(), Translations.getString("application.systemMalfunction.title"))
			}
		}
		textField.alignmentX = Component.LEFT_ALIGNMENT

		contentPanel.add(textField)
		contentPanel.add(Box.createVerticalStrut(10))
		add(contentPanel, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)

		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(JButton(ActionWrapperSwing(exportAction)))
		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		buttonPanel.add(closeButton)
		buttonPanel.add(Box.createHorizontalStrut(5))
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun createErrorIcon(): JComponent {
		val icon = UIManager.getIcon("OptionPane.errorIcon")
		return JLabel(icon, SwingConstants.LEFT)
	}

	private inner class CloseAction : AbstractAction("base.action.close") {
		override fun execute(event: ActionEvent) {
			closeHandler()
		}
	}

	private inner class ExportAction : AbstractAction("application.systemMalfunction.export") {
		override fun execute(event: ActionEvent) {
			val fileChooser = JFileChooser()
			fileChooser.dialogTitle = name
			fileChooser.dialogType = JFileChooser.SAVE_DIALOG
			fileChooser.selectedFile = File("${application.systemName}-dump.zip")

			if (fileChooser.showSaveDialog(this@SystemMalfunctionPanel) != JFileChooser.APPROVE_OPTION) {
				return
			}

			try {
				LOG.userTrail("Exporting system dump")
				val destination = Path.of(fileChooser.selectedFile.absolutePath)
				AppModuleJvm.systemDumpService.createDump(application, destination)

				JOptionPane.showConfirmDialog(
					this@SystemMalfunctionPanel,
					Translations.getString("application.systemMalfunction.export.success.text", destination.toAbsolutePath().toString()),
					name,
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE)
			} catch (e: Throwable) {
				LOG.error("Error while exporting system state", e)

				JOptionPane.showConfirmDialog(
					this@SystemMalfunctionPanel,
					Translations.getString("application.systemMalfunction.export.success.error"),
					name,
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE)
			}
		}
	}
}