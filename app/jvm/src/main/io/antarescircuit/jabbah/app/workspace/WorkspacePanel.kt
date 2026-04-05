package io.antarescircuit.jabbah.app.workspace

import io.antarescircuit.jabbah.app.DesktopApplication
import io.antarescircuit.jabbah.app.Workspace
import io.antarescircuit.jabbah.app.module.AppModuleJvm
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.swing.DataFormPanel
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.swing.FileSelectionField
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
import java.awt.Frame
import java.nio.file.Paths
import javax.swing.*

/** A panel for selecting the current [Workspace]. */
class WorkspacePanel(
	private val application: DesktopApplication,
	private val service: WorkspaceService,
	private val userDataDirectoryPath: String,
	private val initMode: Boolean,
	introText: String? = null,
	initialStatus: String? = null,
	private val closeHandler: () -> Unit
): JPanel() {

	companion object {
		fun showAsDialog(
			title: String,
			parent: Frame?,
			application: DesktopApplication,
			service: WorkspaceService = AppModuleJvm.workspaceService,
			userDataDirectoryPath: String = AppModuleJvm.workspaceHolder.userDataDirectoryPath,
			initMode: Boolean = false,
			introText: String? = null,
			initialStatus: String? = null
		): Boolean {
			val builder = DialogBuilder<WorkspacePanel>(parent)
				.title(title)
				.content { dialog -> WorkspacePanel(application, service, userDataDirectoryPath, initMode, introText, initialStatus)  { dialog.dispose() } }
				.defaultButton { it.okButton }
				.nonResizable()
				.show()

			return builder.content.okPressed
		}
	}

	private val okAction = OkAction()
	private val okButton = createButton(okAction)
	private val cancelAction = CancelAction()

	private val directorySelectionField = FileSelectionField(
		text = userDataDirectoryPath,
		labelText = null
	) {
		setStatus(null)
	}

	private val defaultCheckBox = JCheckBox()

	private val statusField = JLabel(" ", SwingConstants.LEADING)

	private var okPressed = false

	init {
		buildUI(introText)
		setStatus(initialStatus)
		defaultCheckBox.addActionListener { handleDefaultCheckbox() }
	}

	private fun buildUI(introText: String?) {
		layout = BorderLayout(10, 20)
		border = UIBasics.createDialogBorder()

		introText?.let {
			add(buildIntroComponent(it), BorderLayout.NORTH)
		}
		add(buildContentPanel(), BorderLayout.CENTER)
		add(buildButtonPanel(), BorderLayout.SOUTH)
	}

	private fun buildIntroComponent(text: String): JComponent {
		val textArea = JTextArea(text)
		textArea.border = BorderFactory.createEmptyBorder(5, DataFormPanel.DEF_INSET, 0, 0)
		textArea.isEditable = false
		textArea.lineWrap = true
		textArea.wrapStyleWord = true
		return textArea
	}

	private fun buildContentPanel(): JPanel {
		val contentPanel = JPanel(BorderLayout())
		val dataFormPanel = DataFormPanel()

		dataFormPanel.addLabeledRow(Translations.getString("application.workspace.defaultLocation"), defaultCheckBox)
		dataFormPanel.addLabeledRow(Translations.getString("application.workspace.label"), directorySelectionField, true)

		contentPanel.add(dataFormPanel, BorderLayout.CENTER)

		statusField.border = BorderFactory.createEmptyBorder(10, dataFormPanel.leftInset, 0, 0)
		statusField.foreground = UiUtil.errorTextColor
		contentPanel.add(statusField, BorderLayout.SOUTH)

		return contentPanel
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
			directorySelectionField.path = userDataDirectoryPath
		}
		setStatus(null)
	}

	private fun setDefault() {
		directorySelectionField.selectionEnabled = false
	}

	private fun setNonDefault() {
		directorySelectionField.selectionEnabled = true
	}

	private fun setStatus(status: String?) {
		if (status != null) {
			statusField.text = status
			okAction.enabled = false
		} else {
			statusField.text = ""
			okAction.enabled = true
		}
	}

	private fun createButton(action: Action): JButton =
		JButton(ActionWrapperSwing(action))

	private fun open(path: String) {
		try {
			if (initMode) {
				service.initializeWorkspace(Paths.get(path))
			} else {
				service.setWorkspace(Paths.get(path))
			}
			closeHandler()
		} catch (e: java.lang.Exception) {
			setStatus(e.message)
		}
	}

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			okPressed = true
			open(directorySelectionField.path)
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			okPressed = false
			closeHandler()
		}
	}
}