package ch.scorpion.jabbah.app.workspace

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.Workspace
import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DataFormPanel
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.FileSelectionField
import ch.scorpion.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.nio.file.Paths
import javax.swing.*

/** A panel for selecting the current [Workspace]. */
class WorkspacePanel(
	private val application: DesktopApplication,
	private val service: WorkspaceService,
	private val closeHandler: () -> Unit
): JPanel() {

	companion object {
		fun showAsDialog(
			title: String,
			parent: Frame,
			application: DesktopApplication,
			service: WorkspaceService = AppModuleJvm.workspaceService
		) {
			DialogBuilder<WorkspacePanel>(parent)
				.title(title)
				.content { dialog -> WorkspacePanel(application, service)  { dialog.dispose() } }
				.defaultButton { it.okButton }
				.preferredSize(Dimension(500, 150))
				.nonResizable()
				.show()
		}
	}

	private val okAction = OkAction()
	private val okButton = createButton(okAction)
	private val cancelAction = CancelAction()

	private val directorySelectionField = FileSelectionField(
		text = AppModuleJvm.workspaceHolder.userDataDirectoryPath,
		labelText = null
	)

	private val defaultCheckBox = JCheckBox()

	init {
		buildUI()
		defaultCheckBox.addActionListener { handleDefaultCheckbox() }
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		add(buildContentPanel(), BorderLayout.NORTH)
		add(buildButtonPanel(), BorderLayout.SOUTH)
	}

	private fun buildContentPanel(): JPanel {
		val panel = DataFormPanel()

		panel.addLabeledRow(Translations.getString("application.workspace.defaultLocation"), defaultCheckBox)
		panel.addLabeledRow(Translations.getString("application.workspace.label"), directorySelectionField, true)

		return panel
	}

	private fun buildButtonPanel(): JPanel {
		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(buttonPanel, okButton, createButton(cancelAction))
		return buttonPanel
	}

	private fun handleDefaultCheckbox() {
		if (defaultCheckBox.isSelected) {
			setDefault()
			directorySelectionField.path = application.defaultUserDataDirectoryPath.toAbsolutePath().toString()
		} else {
			setNonDefault()
			directorySelectionField.path = AppModuleJvm.workspaceHolder.userDataDirectoryPath
		}
	}

	private fun setDefault() {
		directorySelectionField.selectionEnabled = false
	}

	private fun setNonDefault() {
		directorySelectionField.selectionEnabled = true
	}

	private fun createButton(action: Action): JButton =
		JButton(ActionWrapperSwing(action))

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			service.openWorkspace(Paths.get(directorySelectionField.path))
			closeHandler()
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			closeHandler()
		}
	}
}