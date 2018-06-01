package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Frame
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * Displays a list of all existing project names and allows the user to open a project.
 */
class ProjectPersistencePanel(
	private val service: ProjectPersistenceService = ProjectModule.projectPersistenceService,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private val LOG by logger(ProjectPersistencePanel::class)

		fun showAsDialog(parent: JFrame) {
			val dialog = JDialog(parent, true)
			dialog.title = Translations.getString("project.dialog.title")
			dialog.contentPane.add(ProjectPersistencePanel(closeHandler = { dialog.isVisible = false }))
			dialog.pack()
			dialog.setLocationRelativeTo(parent)
			dialog.isVisible = true
		}
	}

	private val projectNameList = JList<String>(loadProjectNames())
	private val openAction = OpenAction()

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
		openAction.enabled = !projectNameList.isSelectionEmpty
	}

	private fun buildUI() {
		layout = BorderLayout()
		preferredSize = Dimension(300, 300)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		val scrollPane = JScrollPane(projectNameList)
		add(scrollPane, BorderLayout.CENTER)

		val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
		buttonPanel.add(JButton(ActionWrapperSwing(openAction)))
		buttonPanel.add(JButton(ActionWrapperSwing(NewAction())))
		buttonPanel.add(JButton(ActionWrapperSwing(CancelAction())))
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun loadProjectNames(): ListModel<String> {
		val list = DefaultListModel<String>()
		service.getProjectNames().forEach { list.addElement(it) }
		return list
	}

	private val selectedProjectName: String? get() = projectNameList.selectedValue

	private inner class OpenAction : AbstractAction("project.dialog.open.action") {
		override fun execute(event: ActionEvent) {
			LOG.debug("ProjectPersistencePanel: open project '$selectedProjectName'")
			closeHandler.invoke()
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
			while (true) {
				val name = JOptionPane.showInputDialog(
					this@ProjectPersistencePanel,
					Translations.getString("project.dialog.new.name.dialog.desc"),
					Translations.getString("project.dialog.new.name.dialog.title"),
					JOptionPane.QUESTION_MESSAGE
				)
				if (StringUtils.isEmpty(name)) {
					return
				}
				if (service.exists(name)) {
					if (JOptionPane.showConfirmDialog(
						this@ProjectPersistencePanel,
						Translations.getString("project.dialog.new.duplicate.msg", name),
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

			LOG.debug("ProjectPersistencePanel: creating new project '$name'")
			closeHandler.invoke()
		}
	}
}