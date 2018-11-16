package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.*
import java.awt.event.*

import javax.swing.*

/** Opens and shows the [ProjectPersistencePanel] in a modal dialog.*/
class ShowProjectsDialogAction(
	private val parent: JFrame
) : AbstractAction("project.dialog.action") {

	override fun execute(event: ActionEvent) {
		ProjectPersistencePanel.showAsDialog(parent)
	}
}

/**
 * Displays a list of all existing project names and allows the user to open a project.
 */
class ProjectPersistencePanel(
	private val service: ProjectService = ProjectModule.projectService,
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
		projectNameList.addMouseListener(object: MouseAdapter() {
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

		val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
		buttonPanel.add(JButton(ActionWrapperSwing(openAction)))
		buttonPanel.add(JButton(ActionWrapperSwing(NewAction())))
		buttonPanel.add(JButton(ActionWrapperSwing(deleteAction)))
		buttonPanel.add(JButton(ActionWrapperSwing(CancelAction())))
		add(buttonPanel, BorderLayout.SOUTH)

		projectNameList.cellRenderer = ProjectListRenderer()
	}

	private fun loadProjectNames(): ListModel<String> {
		val list = DefaultListModel<String>()
		service.getProjectNames().forEach { list.addElement(it) }
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
				service.open(projectNameList.selectedValue)
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
			var projectName: String?
			while (true) {
				projectName = JOptionPane.showInputDialog(
					this@ProjectPersistencePanel,
					Translations.getString("project.dialog.new.name.dialog.desc"),
					Translations.getString("project.dialog.new.name.dialog.title"),
					JOptionPane.QUESTION_MESSAGE
				)
				if (StringUtils.isEmpty(projectName)) {
					return
				}
				if (service.exists(projectName)) {
					if (JOptionPane.showConfirmDialog(
						this@ProjectPersistencePanel,
						Translations.getString("project.dialog.new.duplicate.msg", projectName),
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

			LOG.debug("ProjectPersistencePanel: creating new project '$projectName'")
			service.open(service.create(projectName!!))
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
				service.delete(selectedProjectName!!)
				refreshProjectNames()
			}
		}
	}
}