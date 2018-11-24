package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import java.awt.Frame
import javax.swing.Action
import javax.swing.JOptionPane

/** An [Action] for changing the [LibraryProperties] of a [Library].*/
class LibraryPropertiesAction(
	private val managementService: LibraryManagementService = LibraryModule.libraryManagementService,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder
) : AbstractAction("library.action.properties") {

	override fun execute(event: ActionEvent) {
		var properties: LibraryProperties? = libraryHolder.library.properties
		val title = Translations.getString("library.dialog.properties.title", properties!!.name)
		while (true) {
			properties = LibraryPropertiesPanel.showAsDialog(title = title, properties = properties)
			if (properties == null) {
				return
			}
			if (StringUtils.isBlank(properties.name)) {
				if (JOptionPane.showConfirmDialog(
					Frame.getFrames()[0],
					Translations.getString("library.emptyName.msg"),
					title,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
				) {
					return
				}
			} else if (managementService.exists(properties.name)) {
				if (JOptionPane.showConfirmDialog(
					Frame.getFrames()[0],
					Translations.getString("library.duplicate.msg", properties.name),
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