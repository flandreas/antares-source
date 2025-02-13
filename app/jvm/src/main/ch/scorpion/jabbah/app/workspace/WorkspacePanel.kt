package ch.scorpion.jabbah.app.workspace

import ch.scorpion.jabbah.app.Workspace
import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
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
	private val service: WorkspaceService,
	private val closeHandler: () -> Unit
): JPanel() {

	companion object {
		fun showAsDialog(
			title: String,
			parent: Frame,
			service: WorkspaceService = AppModuleJvm.workspaceService
		) {
			DialogBuilder<WorkspacePanel>(parent)
				.title(title)
				.content { dialog -> WorkspacePanel(service)  { dialog.dispose() } }
				.defaultButton { it.okButton }
				.preferredSize(Dimension(400, 150))
				.nonResizable()
				.show()
		}
	}

	private val okAction = OkAction()
	private val okButton = createButton(okAction)
	private val cancelAction = CancelAction()

	private val directorySelectionField = FileSelectionField(
		text = AppModuleJvm.workspaceHolder.userDataDirectoryPath,
		labelText = Translations.getString("application.workspace.label")
	)

	init {
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()
		directorySelectionField.border = BorderFactory.createEmptyBorder(10, 10, 10, 0)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(buttonPanel, okButton, createButton(cancelAction))

		add(directorySelectionField, BorderLayout.NORTH)
		add(buttonPanel, BorderLayout.SOUTH)
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