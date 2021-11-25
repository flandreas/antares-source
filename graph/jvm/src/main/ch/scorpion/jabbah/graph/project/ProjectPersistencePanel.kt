package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.library.AbstractLibraryPersistencePanel
import ch.scorpion.jabbah.graph.library.LibraryProperties
import ch.scorpion.jabbah.graph.library.LibraryPropertiesPanel
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.ui.AbstractApplicationModeEditAction
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/** Opens and shows the [ProjectPersistencePanel] in a modal dialog.*/
class ShowProjectsDialogAction(
	applicationModeHolder: ApplicationModeHolder,
	private val parent: JFrame
) : AbstractApplicationModeEditAction("project.dialog.action", applicationModeHolder) {

	override fun execute(event: ActionEvent) {
		ProjectPersistencePanel.showAsDialog(parent)
	}

	override fun calculateEnabledness(): Boolean = true
}

/**
 * Displays a list of all existing project names and allows the user to open a project.
 */
class ProjectPersistencePanel(
	private val managementService: ProjectManagementService = ProjectModule.projectManagementService.invoke(),
	private val projectHolder: ProjectHolder = ProjectModule.projectHolder,
	private val closeHandler: () -> Unit
) : AbstractLibraryPersistencePanel(managementService, "project") {

	companion object {

		private val LOG by logger(ProjectPersistencePanel::class)

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
	override val selectedLibrary: LibraryDictionaryEntry? get() = projectsList.selectedValue
	private val currentProjectFont = projectsList.font.deriveFont(Font.BOLD)
	private val openAction = OpenAction()
	private val deleteAction = DeleteAction()
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

		projectsList.requestFocusInWindow()
		currentProjectIndex()?.let { projectsList.selectedIndex = it }
	}

	override fun getExportSuccessMsg(entry: LibraryDictionaryEntry): String =
		Translations.getString("project.dialog.export.success.msg", entry.name.value)

	override fun getImportSuccessMsg(name: String): String =
		Translations.getString("project.dialog.import.success.msg", name)

	override fun getAlreadyExistsMsg(name: String): String =
		Translations.getString("project.dialog.import.alreadyExists.msg", name)

	override fun getInvalidMsg(name: String): String =
		Translations.getString("project.dialog.import.invalid.msg", name)

	override fun getStaleReferenceMsg(name: String): String =
		Translations.getString("project.dialog.import.staleLibraryReference.msg", name)

	override fun getUuidAlreadyExistsMsg(): String =
		Translations.getString("project.dialog.import.uuidAlreadyExists.msg")

	override val exportActionNameKey: String get() = "project.dialog.export.action"

	override val importActionNameKey: String get() = "project.dialog.import.action"

	override val fileExtensionFilterName: String
		get() = Translations.getString("project.dialog.import.filter.name")

	private fun currentProjectIndex(): Int? {
		if (projectsList.model.size == 0) {
			return null
		}
		for (index in 0 until projectsList.model.size) {
			if (projectsList.model.getElementAt(index).uuid == projectHolder.project?.uuid) {
				return index
			}
		}
		return null
	}

	private fun updateActions() {
		openAction.enabled = selectedLibrary != null && selectedLibrary?.uuid != projectHolder.p?.uuid
		deleteAction.enabled = !projectsList.isSelectionEmpty
		exportAction.enabled = !projectsList.isSelectionEmpty
		importAction.enabled = true
	}

	private fun updateDescription() {
		descriptionTextArea.text = selectedLibrary?.description?.value ?: ""
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

	override fun refreshLibraries() {
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
			selectedLibrary?.let {
				LOG.debug("open project '${it.uuid}'")
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
			LOG.trace("create new project")
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
				} else if (managementService.existsName(properties.name)) {
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

			LOG.debug("Create new project '${properties!!.name.getTranslation()}'")
			val project = managementService.create(properties)
			LOG.debug("Created new project ${project.uuid}")

			managementService.open(project)
			closeHandler.invoke()
		}
	}

	private inner class DeleteAction : AbstractAction("project.dialog.delete.action") {
		override fun execute(event: ActionEvent) {
			selectedLibrary?.let {
				if (JOptionPane.showConfirmDialog(
						this@ProjectPersistencePanel,
						Translations.getString("project.dialog.delete.confirm.msg", it.name.value),
						Translations.getString("project.dialog.delete.action.name"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION
				) {
					LOG.debug("Delete project '${it.uuid}'")
					managementService.delete(it.uuid)
					refreshLibraries()
				}
			}
		}
	}
}