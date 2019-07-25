package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.graph.library.LibraryProperties
import ch.scorpion.jabbah.graph.library.LibraryPropertiesPanel
import ch.scorpion.jabbah.graph.ui.AbstractApplicationModeEditAction
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

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

		fun showAsDialog(parent: JFrame) {
			val dialog = JDialog(parent, true)
			BusyHandler.register(dialog, null)
			dialog.title = Translations.getString("project.dialog.title")
			dialog.contentPane.add(ProjectPersistencePanel(closeHandler = {
				dialog.isVisible = false
				dialog.dispose()
			}))
			dialog.pack()
			dialog.setLocationRelativeTo(parent)
			dialog.addWindowListener(object : WindowAdapter() {
				override fun windowClosed(e: WindowEvent?) {
					BusyHandler.deregister(dialog)
				}
			})
			dialog.isVisible = true
		}
	}

	private val projectNameList = JList<String>(loadProjectNames())
	private val currentProjectFont = projectNameList.font.deriveFont(Font.BOLD)
	private val openAction = OpenAction()
	private val deleteAction = DeleteAction()

	init {
		projectNameList.addListSelectionListener { updateActions() }
		projectNameList.addMouseListener(object : MouseAdapter() {
			override fun mouseClicked(e: MouseEvent?) {
				if (e!!.clickCount == 2) {
					openAction.execute(ActionWrapperSwing.toJabbaActionEvent(e))
				}
			}
		})
		buildUI()
		updateActions()
	}

	private fun updateActions() {
		openAction.enabled = !projectNameList.isSelectionEmpty && projectNameList.selectedValue != projectHolder.p?.name
		deleteAction.enabled = !projectNameList.isSelectionEmpty
	}

	private fun buildUI() {
		layout = BorderLayout()
		preferredSize = Dimension(400, 500)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		val scrollPane = JScrollPane(projectNameList)
		add(scrollPane, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(JButton(ActionWrapperSwing(openAction)))
		buttonPanel.add(JButton(ActionWrapperSwing(NewAction())))
		buttonPanel.add(JButton(ActionWrapperSwing(deleteAction)))

		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(JButton(ActionWrapperSwing(CancelAction())))
		add(buttonPanel, BorderLayout.SOUTH)

		projectNameList.cellRenderer = ProjectListRenderer()
	}

	private fun loadProjectNames(): ListModel<String> {
		val list = DefaultListModel<String>()
		managementService.getProjectNames().forEach { list.addElement(it) }
		return list
	}

	private fun refreshProjectNames() {
		projectNameList.model = loadProjectNames()
	}

	private val selectedProjectName: String? get() = projectNameList.selectedValue

	private inner class ProjectListRenderer : DefaultListCellRenderer() {

		override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
			val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
			val name = value as String
			if (name == projectHolder.project?.name) {
				renderer.font = currentProjectFont
			} else {
				renderer.font = projectNameList.font
			}
			return renderer
		}
	}

	private inner class OpenAction : AbstractAction("project.dialog.open.action") {
		override fun execute(event: ActionEvent) {
			LOG.debug("ProjectPersistencePanel: open project '$selectedProjectName'")
			InvocationHandler.invoke {
				managementService.open(projectNameList.selectedValue)
				closeHandler.invoke()
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
			LOG.debug("ProjectPersistencePanel: new project")
			var properties: LibraryProperties?
			while (true) {
				properties = LibraryPropertiesPanel.showAsDialog(title = Translations.getString("project.dialog.new.dialog.title"))
				if (properties == null) {
					return
				}
				if (StringUtils.isBlank(properties.name)) {
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
							Translations.getString("project.duplicate.msg", properties.name),
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

			LOG.debug("ProjectPersistencePanel: creating new project '${properties!!.name}'")
			managementService.open(managementService.create(properties))
			closeHandler.invoke()
		}
	}

	private inner class DeleteAction : AbstractAction("project.dialog.delete.action") {
		override fun execute(event: ActionEvent) {
			if (JOptionPane.showConfirmDialog(
					this@ProjectPersistencePanel,
					Translations.getString("project.dialog.delete.confirm.msg", selectedProjectName!!),
					Translations.getString("project.dialog.delete.action.name"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION
			) {
				managementService.delete(selectedProjectName!!)
				refreshProjectNames()
			}
		}
	}
}