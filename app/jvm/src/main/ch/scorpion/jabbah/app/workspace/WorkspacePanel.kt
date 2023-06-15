package ch.scorpion.jabbah.app.workspace

import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.app.Workspace
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
import java.awt.Component
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
			parent: Frame,
			service: WorkspaceService = AppModuleJvm.workspaceService
		) {
			DialogBuilder<WorkspacePanel>(parent)
				.title(Translations.getString("application.workspace.dialog.title"))
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
	private val label = JLabel(Translations.getString("application.workspace.label"))
	private val workspacePath = JTextField()
	private val selectAction = SelectAction()
	private val selectButton = createButton(selectAction)

	init {
		buildUI()
		workspacePath.text = AppModuleJvm.workspaceHolder.userDataDirectoryPath
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		val contentPanel = JPanel()
		contentPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 0)
		contentPanel.layout = BoxLayout(contentPanel, BoxLayout.LINE_AXIS)
		contentPanel.add(label)
		contentPanel.add(Box.createHorizontalStrut(5))
		contentPanel.add(workspacePath)
		contentPanel.add(Box.createHorizontalStrut(5))
		contentPanel.add(selectButton)

		label.alignmentX = Component.LEFT_ALIGNMENT
		workspacePath.alignmentX = Component.LEFT_ALIGNMENT
		selectButton.alignmentX = Component.LEFT_ALIGNMENT

		workspacePath.isEditable = false
		workspacePath.preferredSize = Dimension(300, workspacePath.preferredSize.height)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(buttonPanel, okButton, createButton(cancelAction))

		add(contentPanel, BorderLayout.NORTH)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun createButton(action: Action): JButton =
		JButton(ActionWrapperSwing(action))

	private inner class SelectAction : AbstractAction("application.workspace.select") {
		override fun execute(event: ActionEvent) {
			val fileChooser = JFileChooser(workspacePath.text);
			fileChooser.dialogTitle = Translations.getString("application.workspace.select.title")
			fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

			if (fileChooser.showOpenDialog(this@WorkspacePanel) == JFileChooser.APPROVE_OPTION) {
				workspacePath.text = fileChooser.selectedFile.absolutePath
			}
		}
	}

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			service.openWorkspace(Paths.get(workspacePath.text))
			closeHandler()
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			closeHandler()
		}
	}
}