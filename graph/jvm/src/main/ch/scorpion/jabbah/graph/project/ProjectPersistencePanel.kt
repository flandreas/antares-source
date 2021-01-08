package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.FileExtensionFilter
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.library.LibraryProperties
import ch.scorpion.jabbah.graph.library.LibraryPropertiesPanel
import ch.scorpion.jabbah.graph.project.ProjectImportResult.*
import ch.scorpion.jabbah.graph.ui.AbstractApplicationModeEditAction
import org.apache.commons.io.FilenameUtils
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileFilter

/** Opens and shows the [ProjectPersistencePanel] in a modal dialog.*/
class ShowProjectsDialogAction(
	private val parent: JFrame
) : AbstractApplicationModeEditAction("project.dialog.action") {

	override fun execute(event: ActionEvent) {
		ProjectPersistencePanel.showAsDialog(parent)
	}

	override fun calculateEnabledness(): Boolean = true
}

/**
 * Displays a list of all existing project names and allows the user to open a project.
 */
class ProjectPersistencePanel(
	private val managementService: ProjectManagementService = ProjectModule.projectManagementService,
	private val projectHolder: ProjectHolder = ProjectModule.projectHolder,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private val LOG by logger(ProjectPersistencePanel::class)

		private const val EXPORT_FILE_EXTENSION = "zip"

		fun showAsDialog(parent: JFrame) {
			DialogBuilder<ProjectPersistencePanel>(parent)
				.content { dialog -> ProjectPersistencePanel(closeHandler = { dialog.dispose() }) }
				.title(Translations.getString("project.dialog.title"))
				.defaultButton { it.openButton }
				.nonResizable()
				.show()
		}
	}

	private val projectsList = JList(loadProjects())
	private val descriptionTextArea = JTextArea()
	private val selectedProject: LibraryDictionaryEntry? get() = projectsList.selectedValue
	private val currentProjectFont = projectsList.font.deriveFont(Font.BOLD)
	private val openAction = OpenAction()
	private val deleteAction = DeleteAction()
	private val exportAction = ExportAction()
	private val importAction = ImportAction()
	val openButton = createButton(openAction)

	init {
		projectsList.addListSelectionListener {
			updateDescription()
			updateActions()
		}
		projectsList.addMouseListener(object : MouseAdapter() {
			override fun mouseClicked(e: MouseEvent?) {
				if (e!!.clickCount == 2) {
					openAction.execute(ActionWrapperSwing.toJabbahActionEvent(e))
				}
			}
		})
		buildUI()
		updateActions()
	}

	private fun updateActions() {
		openAction.enabled = selectedProject != null && selectedProject?.uuid != projectHolder.p?.uuid
		deleteAction.enabled = !projectsList.isSelectionEmpty
		exportAction.enabled = !projectsList.isSelectionEmpty
		importAction.enabled = true
	}

	private fun updateDescription() {
		descriptionTextArea.text = selectedProject?.description?.value ?: ""
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		descriptionTextArea.lineWrap = true
		descriptionTextArea.wrapStyleWord = true
		descriptionTextArea.background = background
		descriptionTextArea.isEditable = false
		descriptionTextArea.rows = 6
		val descriptionScroll = UiUtil.decorateTextArea(descriptionTextArea)
		descriptionScroll.background = background

		val scrollPane = JScrollPane(projectsList)
		scrollPane.preferredSize = Dimension(300, 300)
		add(scrollPane, BorderLayout.NORTH)

		add(descriptionScroll, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)

		buttonPanel.add(openButton)
		buttonPanel.add(Box.createHorizontalStrut(2))
		buttonPanel.add(createButton(NewAction()))
		buttonPanel.add(Box.createHorizontalStrut(2))
		buttonPanel.add(createButton(deleteAction))
		buttonPanel.add(Box.createHorizontalStrut(9))
		buttonPanel.add(createButton(exportAction))
		buttonPanel.add(Box.createHorizontalStrut(2))
		buttonPanel.add(createButton(importAction))
		buttonPanel.add(Box.createHorizontalStrut(2))
		buttonPanel.add(Box.createHorizontalStrut(9))
		buttonPanel.add(createButton(CancelAction()))

		add(buttonPanel, BorderLayout.SOUTH)

		projectsList.cellRenderer = ProjectListRenderer()
	}

	private fun createButton(action: Action): JButton {
		return JButton(ActionWrapperSwing(action))
	}

	private fun loadProjects(): ListModel<LibraryDictionaryEntry> {
		val list = DefaultListModel<LibraryDictionaryEntry>()
		managementService
			.getProjectDirectoryEntries()
			.sortedBy { it.name.value }
			.forEach { list.addElement(it) }
		return list
	}

	private fun refreshProjects() {
		projectsList.model = loadProjects()
	}

	private inner class ProjectListRenderer : DefaultListCellRenderer() {

		override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
			val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
			val project = value as LibraryDictionaryEntry
			if (project.uuid == projectHolder.project?.uuid) {
				renderer.font = currentProjectFont
			} else {
				renderer.font = projectsList.font
			}
			return renderer
		}
	}

	private inner class OpenAction : AbstractAction("project.dialog.open.action") {
		override fun execute(event: ActionEvent) {
			selectedProject?.let {
				LOG.debug(" open project '${it.uuid}'")
				InvocationHandler.invoke {
					managementService.open(it.uuid)
					closeHandler.invoke()
				}
			}
		}
	}

	private inner class CancelAction : AbstractAction("project.dialog.cancel.action") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}

	private inner class NewAction : AbstractAction("project.dialog.new.action") {
		override fun execute(event: ActionEvent) {
			LOG.debug("new project")
			var properties: LibraryProperties?
			while (true) {
				properties = LibraryPropertiesPanel.showAsDialog(
					parent = this@ProjectPersistencePanel,
					title = Translations.getString("project.dialog.new.dialog.title"))
				if (properties == null) {
					return
				}
				if (StringUtils.isBlank(properties.name.getTranslation())) {
					if (JOptionPane.showConfirmDialog(
							this@ProjectPersistencePanel,
							Translations.getString("project.emptyName.msg"),
							Translations.getString("project.dialog.new.dialog.title"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
					) {
						return
					}
				} else if (managementService.exists(properties.name)) {
					if (JOptionPane.showConfirmDialog(
							this@ProjectPersistencePanel,
							Translations.getString("project.duplicate.msg", properties.name.getTranslation()),
							Translations.getString("project.dialog.new.name.dialog.title"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
					) {
						return
					}
				} else {
					break
				}
			}

			LOG.debug("creating new project '${properties!!.name.getTranslation()}'")
			managementService.open(managementService.create(properties))
			closeHandler.invoke()
		}
	}

	private inner class DeleteAction : AbstractAction("project.dialog.delete.action") {
		override fun execute(event: ActionEvent) {
			selectedProject?.let {
				if (JOptionPane.showConfirmDialog(
						this@ProjectPersistencePanel,
						Translations.getString("project.dialog.delete.confirm.msg", it.name.value),
						Translations.getString("project.dialog.delete.action.name"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION
				) {
					managementService.delete(it.uuid)
					refreshProjects()
				}
			}
		}
	}

	private inner class ExportAction : AbstractAction("project.dialog.export.action") {
		override fun execute(event: ActionEvent) {
			selectedProject?.let {
				val fileChooser = JFileChooser()
				fileChooser.dialogTitle = name
				fileChooser.selectedFile = File("${it.name.value}.$EXPORT_FILE_EXTENSION")
				if (fileChooser.showSaveDialog(this@ProjectPersistencePanel) == JFileChooser.APPROVE_OPTION) {
					managementService.export(it.uuid, fileChooser.selectedFile.absolutePath)
					JOptionPane.showConfirmDialog(
						this@ProjectPersistencePanel,
						Translations.getString("project.dialog.export.success.msg", it.name.value),
						name,
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE)
				}
			}
		}
	}

	private inner class ImportAction : AbstractAction("project.dialog.import.action") {
		override fun execute(event: ActionEvent) {
			val fileChooser = JFileChooser()
			fileChooser.dialogTitle = name
			fileChooser.fileFilter = createFilter()
			if (fileChooser.showOpenDialog(this@ProjectPersistencePanel) == JFileChooser.APPROVE_OPTION) {
				import(fileChooser.selectedFile.absolutePath)
			}
		}

		private fun import(path: String) {
			val name = FilenameUtils.getBaseName(path)
			when (managementService.import(path)) {
				Success -> handleSuccessfulImport(name)
				NameAlreadyExists -> handleImportNameAlreadyExists(name)
				Invalid -> handleInvalidImportFile(name)
				StaleLibraryReference -> handleStaleLibraryReference(name)
			}
		}

		private fun createFilter(): FileFilter {
			return FileExtensionFilter(EXPORT_FILE_EXTENSION, Translations.getString("project.dialog.import.filter.name"))
		}

		fun handleSuccessfulImport(projectName: String) {
			JOptionPane.showConfirmDialog(
				this@ProjectPersistencePanel,
				Translations.getString("project.dialog.import.success.msg", projectName),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE)
			refreshProjects()
		}

		fun handleImportNameAlreadyExists(projectName: String) {
			JOptionPane.showConfirmDialog(
				this@ProjectPersistencePanel,
				Translations.getString("project.dialog.import.alreadyExists.msg", projectName),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE)
		}

		fun handleInvalidImportFile(projectName: String) {
			JOptionPane.showConfirmDialog(
				this@ProjectPersistencePanel,
				Translations.getString("project.dialog.import.invalid.msg", projectName),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE)
		}

		fun handleStaleLibraryReference(projectName: String) {
			JOptionPane.showConfirmDialog(
				this@ProjectPersistencePanel,
				Translations.getString("project.dialog.import.staleLibraryReference.msg", projectName),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE)
		}
	}

}