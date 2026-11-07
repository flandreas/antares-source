package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.edit.auth.Authorizer
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame
import javax.swing.JOptionPane

/** An [Action] for changing the [LibraryProperties] of a [Library].*/
abstract class AbstractLibraryPropertiesAction(
	baseName: String,
	controller: LibraryTreeViewController
) : AbstractLibraryAction(baseName, Operation.View, controller) {

	/** ---- [AbstractLibraryAction] */

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && selectedItem is Library

	/** ---- [AbstractLibraryPropertiesAction] */

	protected abstract val dialogTitle: String

	protected abstract fun exists(newName: TranslatableText): Boolean

	protected abstract fun update(properties: LibraryProperties)

	protected val library: Library get() = selectedItem as Library

	protected val currentProperties: LibraryProperties get() = library.properties

	private val currentPreferences: LibraryPreferences get() = library.preferences

	private val isEditable: Boolean get() = Authorizer.isCurrentUserAuthorizedTo(Operation.Change, library)

	private val isSystem: Boolean get() = library.isSystem

	private val emptyMessage: String get() = Translations.getString("library.emptyName.msg")

	private fun duplicateMessage(newName: String): String = Translations.getString("library.duplicate.msg", newName)

	/** ---- [Action] interface */

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
				libraryPreferences = currentPreferences,
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
		update(properties)
	}
}