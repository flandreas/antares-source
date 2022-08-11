package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import java.awt.Frame
import javax.swing.Action
import javax.swing.JOptionPane

/** An [Action] for changing the [LibraryProperties] of a [Library].*/
abstract class AbstractLibraryPropertiesAction(
	baseName: String
) : AbstractAction(baseName) {

	protected abstract val isEditable: Boolean
	protected abstract val isSystem: Boolean
	protected abstract val currentProperties: LibraryProperties
	protected abstract val dialogTitle: String
	protected abstract val emptyMessage: String
	protected abstract fun duplicateMessage(newName: String): String
	protected abstract fun exists(newName: TranslatableText): Boolean
	protected abstract fun update(properties: LibraryProperties)

	override fun execute(event: ActionEvent) {
		var properties: LibraryProperties? = currentProperties
		val title = dialogTitle
		while (true) {
			properties = LibraryPropertiesPanel.showAsDialog(
				title = title,
				supportOwnership = true,
				supportImport = false,
				isSystem = isSystem,
				properties = properties,
				editable = isEditable
			)
			if (properties == null) {
				return
			}
			if (StringUtils.isBlank(properties.name.getTranslation())) {
				if (JOptionPane.showConfirmDialog(
					Frame.getFrames()[0],
					emptyMessage,
					title,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
				) {
					return
				}
			} else if (exists(properties.name)) {
				if (JOptionPane.showConfirmDialog(
					Frame.getFrames()[0],
					duplicateMessage(properties.name.getTranslation()),
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
		update(properties!!)
	}
}

class LibraryPropertiesAction(
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	private val managementService: LibraryManagementService = LibraryModule.libraryManagementService
) : AbstractLibraryPropertiesAction(
	baseName = "library.action.properties"
) {
	override val isEditable: Boolean get() = Authorizer.isCurrentUserAuthorizedTo(Operation.Change, libraryHolder.library)

	override val isSystem: Boolean get() = libraryHolder.library.isSystem

	override val currentProperties: LibraryProperties get() = libraryHolder.library.properties

	override val dialogTitle: String get() = Translations.getString("library.dialog.properties.title", currentProperties.name.getTranslation())

	override val emptyMessage: String get() = Translations.getString("library.emptyName.msg")

	override fun duplicateMessage(newName: String): String = Translations.getString("library.duplicate.msg", newName)

	override fun exists(newName: TranslatableText): Boolean {
		return managementService.existsName(newName, except = libraryHolder.library.uuid)
	}

	override fun update(properties: LibraryProperties) {
		managementService.update(properties)
	}
}