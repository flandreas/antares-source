package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.UserIdentity
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.properties.TranslatablePropertyEditor
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.view.LibraryVisibilityEditor
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

/** A [JPanel] for editing the properties of a [Library].*/
class LibraryPropertiesPanel(
	supportOwnership: Boolean,
	isSystem: Boolean,
	properties: LibraryProperties? = null,
	editable: Boolean = true
) : JPanel() {

	companion object {
		fun showAsDialog(
			parent: Component = Frame.getFrames()[0],
			title: String,
			supportOwnership: Boolean,
			isSystem: Boolean,
			properties: LibraryProperties? = null,
			editable: Boolean = true,
		): LibraryProperties? {
			val panel = LibraryPropertiesPanel(supportOwnership, isSystem, properties, editable)
			(panel.nameField.textComponent as JComponent).addAncestorListener(object : AncestorListener {
				override fun ancestorAdded(event: AncestorEvent?) {
					SwingUtilities.invokeLater {
						event?.let {
							it.component.requestFocus()
							it.component.removeAncestorListener(this)
						}
					}
				}
				override fun ancestorRemoved(event: AncestorEvent?) { }
				override fun ancestorMoved(event: AncestorEvent?) { }
			})

			return when (
				JOptionPane.showConfirmDialog(
					parent,
					panel,
					title,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE)
			) {
				JOptionPane.OK_OPTION -> LibraryProperties(
					panel.nameField.value as TranslatableText,
					panel.descField.value as TranslatableText,
					panel.visibilityField.value as LibraryVisibility,
					if (panel.ownedByMeField.isSelected) EditAuthModule.userHolder.user.identity else panel.oldAuthor
				)
				else -> null
			}
		}
	}

	private val nameLabel = Translations.getString("library.property.name.name")
	private val descLabel = Translations.getString("library.property.desc.name")
	val nameField = TranslatablePropertyEditor(nameLabel, editable = editable)
	private val descField = TranslatablePropertyEditor(descLabel, multiline = { true }, rows = 8, editable = editable)
	private val visibilityLabel = Translations.getString("library.property.visibility.name")
	private val visibilityField = LibraryVisibilityEditor()
	private val ownedByMeLabel = Translations.getString("library.property.ownedByMe.name")
	private val ownedByMeField = JCheckBox()
	private val oldAuthor: UserIdentity? = properties?.author

	init {
		preferredSize = Dimension(400, 180)
		visibilityField.customEditor.isEnabled = editable

		ownedByMeField.isEnabled = !isSystem && oldAuthor != EditAuthModule.userHolder.user.identity
		ownedByMeField.addActionListener {
			if (ownedByMeField.isSelected) {
				updateOwnedByMe(confirmOwnByMe())
			} else {
				updateEnabledness(editable)
			}
		}

		buildUI(supportOwnership)

		properties?.let {
			nameField.value = it.name
			descField.value = it.description
			visibilityField.value = it.visibility
			ownedByMeField.isSelected = it.author == EditAuthModule.userHolder.user.identity
		}
	}

	private fun updateOwnedByMe(ownedByMe: Boolean) {
		ownedByMeField.isSelected = ownedByMe
		updateEnabledness(ownedByMe)
	}

	private fun updateEnabledness(enabled: Boolean) {
		nameField.textComponent.isEditable = enabled
		descField.textComponent.isEditable = enabled
		visibilityField.customEditor.isEnabled = enabled
	}

	private fun buildUI(supportOwnership: Boolean) {
		var row = -1
		val inset = 5
		val rowDist = 5
		layout = EGBL.getLayout()

		EGBL.add(
			this,
			JLabel("$nameLabel:"),
			0, ++row,
			1, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			0, inset, 0, 0
		)

		EGBL.add(
			this,
			nameField.customEditor,
			1, row,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.HORIZONTAL,
			0, inset, 0, 0
		)

		EGBL.add(
			this,
			JLabel("$descLabel:"),
			0, ++row,
			1, 1,
			0.0, 0.0,
			EGBL.NORTHWEST,
			EGBL.NONE,
			4 + rowDist, inset, 0, 0
		)

		descField.customEditor.preferredSize = Dimension(descField.customEditor.preferredSize.width, 50)
		EGBL.add(
			this,
			descField.customEditor,
			1, row,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.HORIZONTAL,
			rowDist, inset, 0, 0
		)

		if (supportOwnership) {
			EGBL.add(
				this,
				JLabel("$ownedByMeLabel:"),
				0, ++row,
				1, 1,
				0.0, 0.0,
				EGBL.NORTHWEST,
				EGBL.NONE,
				4 + rowDist, inset, 0, 0
			)

			EGBL.add(
				this,
				ownedByMeField,
				1, row,
				EGBL.REMAINDER, 1,
				0.0, 0.0,
				EGBL.WEST,
				EGBL.HORIZONTAL,
				rowDist, inset, 0, 0
			)
		}

		if (GraphModuleJvm.supportWeb) {
			EGBL.add(
				this,
				JLabel("$visibilityLabel:"),
				0, ++row,
				1, 1,
				0.0, 0.0,
				EGBL.WEST,
				EGBL.NONE,
				rowDist, inset, 0, 0
			)

			EGBL.add(
				this,
				visibilityField.customEditor,
				1, row,
				EGBL.REMAINDER, 1,
				0.0, 0.0,
				EGBL.WEST,
				EGBL.NONE,
				rowDist, inset, 0, 0
			)
		}

		val filler = JPanel()
		EGBL.add(
			this,
			filler,
			10, ++row,
			EGBL.REMAINDER, EGBL.REMAINDER,
			1.0, 1.0,
			EGBL.NORTHWEST,
			EGBL.BOTH
		)
	}

	private fun confirmOwnByMe(): Boolean =
		JOptionPane.showConfirmDialog(
			parent,
			Translations.getString("library.action.ownByMe.text"),
			Translations.getString("library.action.ownByMe.name"),
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.WARNING_MESSAGE
		) == JOptionPane.OK_OPTION
}