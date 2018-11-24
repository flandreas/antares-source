package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.graph.library.LibraryProperties
import ch.scorpion.jabbah.graph.library.LibraryPropertiesPanel
import java.awt.Frame
import javax.swing.Action
import javax.swing.JOptionPane

/** An [Action] for changing the [LibraryProperties] of a [Project].*/
class ProjectPropertiesAction(
	private val managementService: ProjectManagementService = ProjectModule.projectManagementService,
	private val projectHolder: ProjectHolder = ProjectModule.projectHolder
) : AbstractAction("project.action.properties") {

	override fun execute(event: ActionEvent) {
		var properties: LibraryProperties? = projectHolder.project!!.properties
		val title = Translations.getString("project.dialog.properties.title", properties!!.name)
		while (true) {
			properties = LibraryPropertiesPanel.showAsDialog(title = title, properties = properties)
			if (properties == null) {
				return
			}
			if (StringUtils.isBlank(properties.name)) {
				if (JOptionPane.showConfirmDialog(
						Frame.getFrames()[0],
						Translations.getString("project.emptyName.msg"),
						title,
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
				) {
					return
				}
			} else if (managementService.exists(properties.name)) {
				if (JOptionPane.showConfirmDialog(
						Frame.getFrames()[0],
						Translations.getString("project.duplicate.msg", properties.name),
						title,
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
				) {
					return
				}
			} else {
				break
			}

		}
		managementService.update(properties!!)
	}
}