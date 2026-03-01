package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.properties.TranslatablePropertyEditor
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

data class NewMetaGraphInfo(
	val name: TranslatableText,
	val type: GraphType
)

/**
 * A [JPanel] for collecting the input used for creating a new [MetaGraph].
 * @param type the optionally pre-set [GraphType]. If set, the user can't choose it
 */
class NewMetaGraphPanel(
	name: TranslatableText? = null,
	type: GraphType? = null
) : JPanel() {

	companion object {
		fun showAsDialog(
			parent: Component = Frame.getFrames()[0],
			name: TranslatableText? = null,
			type: GraphType? = null
		): NewMetaGraphInfo? {
			val panel = NewMetaGraphPanel(name, type)
			return when (
				JOptionPane.showConfirmDialog(
					parent,
					panel,
					Translations.getString("library.action.newGraph.name"),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE)
				) {
					JOptionPane.OK_OPTION -> NewMetaGraphInfo(panel.nameField.value as TranslatableText, panel.typeField.selectedItem as GraphType)
					else -> null
				}
		}
	}

	private val nameLabel = Translations.getString("library.action.newGraph.property.name")
	private val nameField = TranslatablePropertyEditor(nameLabel)

	private val typeLabel = Translations.getString("library.action.newGraph.property.type")
	private val typeField = JComboBox<GraphType>()

	init {
		fillGraphTypes()
		name?.let { nameField.value = it }
		type?.let {
			typeField.selectedItem = it
			typeField.isEnabled = false
		}

		buildUI()
		requestFocusInNameField()
	}

	private fun fillGraphTypes() {
		GraphModelModule.graphTypeRegistry.graphTypes
			.filter { Authorizer.isCurrentUserAuthorizedTo(Operation.View, it) }
			.forEach { typeField.addItem(it) }
		typeField.selectedItem = GraphModelModule.graphTypeRegistry.default
	}

	private fun requestFocusInNameField() {
		(nameField.textComponent as JComponent).addAncestorListener(object : AncestorListener {
			override fun ancestorAdded(event: AncestorEvent?) {
				SwingUtilities.invokeLater {
					event?.let {
						it.component.requestFocus()
						it.component.removeAncestorListener(this)
					}
				}
			}

			override fun ancestorMoved(event: AncestorEvent?) {}
			override fun ancestorRemoved(event: AncestorEvent?) {}
		})
	}

	private fun buildUI() {
		val inset = 5
		var row = 0
		val rowDist = 5
		layout = EGBL.getLayout()

		// Name

		EGBL.add(
			this,
			JLabel("$nameLabel:"),
			0, row,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			0, inset, 0, 0
		)

		nameField.customEditor.preferredSize = Dimension(width, nameField.customEditor.preferredSize.height)
		nameField.customEditor.minimumSize = Dimension(width, nameField.customEditor.preferredSize.height)

		EGBL.add(
			this,
			nameField.customEditor,
			1, row++,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.HORIZONTAL,
			0, 10, 0, inset
		)

		// Type

		EGBL.add(
			this,
			JLabel("$typeLabel:"),
			0, row,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			rowDist, inset, 0, 0
		)

		EGBL.add(
			this,
			typeField,
			1, row++,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			rowDist, 10, 0, inset
		)

		// Filler

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
}