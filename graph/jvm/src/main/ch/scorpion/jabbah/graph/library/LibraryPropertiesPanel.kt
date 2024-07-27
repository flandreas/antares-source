package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.preferences.Preferences
import ch.scorpion.jabbah.base.preferences.PreferencesMessageDisplay
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.UserIdentity
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.properties.TranslatablePropertyEditor
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.view.LibraryVisibilityEditor
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import java.awt.*
import javax.swing.*
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

/**
 * A [JPanel] for editing the properties of a [Library].
 *
 * @param supportImport `true` if this [LibraryPropertiesPanel] shows a combo box for selecting a [Library]
 * to be imported. Only needed when creating a new [Library] or a new [Project].
 * @param managementService the service for retrieving the [Library] to be imported, if enabled by `supportImport`.
 * Is always [Library], so no need to support the corresponding [Project] management service
 */
class LibraryPropertiesPanel(
	supportOwnership: Boolean,
	supportImport: Boolean,
	isSystem: Boolean,
	properties: LibraryProperties? = null,
	libraryPreferences: LibraryPreferences,
	editable: Boolean = true,
	private val managementService: LibraryManagementService = LibraryModule.libraryManagementService
) : JPanel() {

	companion object {

		fun showAsDialog(
			parent: Component = Frame.getFrames()[0],
			title: String,
			supportOwnership: Boolean,
			supportImport: Boolean,
			isSystem: Boolean,
			properties: LibraryProperties? = null,
			libraryPreferences: LibraryPreferences = LibraryPreferences(),
			editable: Boolean = true,
		): LibraryProperties? {
			val panel = LibraryPropertiesPanel(supportOwnership, supportImport, isSystem, properties, libraryPreferences, editable)

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
					if (panel.ownedByMeField.isSelected) EditAuthModule.userHolder.user.identity else panel.oldAuthor,
					importUuid = (panel.importField.selectedItem as LibraryDictionaryEntry?)?.uuid
				)
				else -> null
			}
		}
	}

	private val nameLabel = Translations.getString("library.property.name.name")
	val nameField = TranslatablePropertyEditor(nameLabel, editable = editable)

	private val descLabel = Translations.getString("library.property.desc.name")
	private val descField = TranslatablePropertyEditor(descLabel, multiline = { true }, rows = 8, editable = editable)

	private val visibilityLabel = Translations.getString("library.property.visibility.name")
	private val visibilityField = LibraryVisibilityEditor()
	private val visibilityDescLabel = JLabel()

	private val importLabel = Translations.getString("library.property.import.name")
	private val importField = JComboBox<LibraryDictionaryEntry>()

	private val ownedByMeLabel = Translations.getString("library.property.ownedByMe.name")
	private val ownedByMeField = JCheckBox()

	private val oldAuthor: UserIdentity? = properties?.author

	/** Bridges [LibraryPreferences] to the [Preferences] class used by the UI.*/
	private val preferences = Preferences(libraryPreferences)

	private val preferenceMessageDisplay = MyMessageDisplay()

	private val preferencesPanel = PreferencesPanel(
		{ GraphViewModuleJvm.libraryPreferencesProvider().iterator() },
		preferences,
		preferenceMessageDisplay,
		addFiller = false
	)

	init {
		preferredSize = Dimension(500, 180)
		visibilityField.customEditor.isEnabled = editable
		importField.isEnabled = editable

		ownedByMeField.isEnabled = !isSystem && oldAuthor != EditAuthModule.userHolder.user.identity
		ownedByMeField.addActionListener {
			if (ownedByMeField.isSelected) {
				updateOwnedByMe(confirmOwnByMe())
			} else {
				updateEnabledness(editable)
			}
		}

		if (supportImport) {
			setupImportComboBox()
		}

		buildUI(supportOwnership, supportImport)

		properties?.let {
			nameField.value = it.name
			descField.value = it.description
			visibilityField.value = it.visibility
			ownedByMeField.isSelected = it.author == EditAuthModule.userHolder.user.identity
		}

		preferencesPanel.load()
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

	private fun setupImportComboBox() {
		val entries = managementService.getLibraryDirectoryEntries()
		entries.forEach {
			importField.addItem(it)
		}
		importField.renderer = CreateLibraryPanel.Companion.LibraryNameRenderer()
		importField.selectedItem = entries.first { it.uuid == LibraryModule.DEF_LIBRARY_UUID }
	}

	override fun paintComponent(g: Graphics?) {
		super.paintComponent(g)
	}

	private fun buildUI(supportOwnership: Boolean, supportImport: Boolean) {
		layout = BorderLayout()

		val propertiesPanel = buildPropertiesPanel(supportOwnership, supportImport)
		propertiesPanel.border = BorderFactory.createEmptyBorder(preferencesPanel.topInset, 0, 20, 0)

		val localPreferencesPanel = buildPreferencesPanel()

		val tabPane = JTabbedPane()
		tabPane.add(Translations.getString("library.dialog.properties.properties"), propertiesPanel)
		tabPane.add(Translations.getString("library.dialog.properties.preferences"), localPreferencesPanel)
		add(tabPane, BorderLayout.CENTER)
	}

	private fun buildPreferencesPanel(): JPanel {
		val panel = JPanel()
		preferencesPanel.alignmentX = Component.LEFT_ALIGNMENT
		preferenceMessageDisplay.alignmentX = Component.LEFT_ALIGNMENT
		preferenceMessageDisplay.border = BorderFactory.createEmptyBorder(10, preferencesPanel.leftInset, 0, 0)
		panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)
		panel.add(preferencesPanel)
		panel.add(preferenceMessageDisplay)
		panel.add(Box.createVerticalStrut(10))
		return panel
	}

	private fun buildPropertiesPanel(supportOwnership: Boolean, supportImport: Boolean): JPanel {
		val panel = JPanel()
		var row = -1
		val inset = 5
		val rowDist = 5
		panel.layout = EGBL.getLayout()

		EGBL.add(
			panel,
			JLabel("$nameLabel:"),
			0, ++row,
			1, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			0, inset, 0, 0
		)

		EGBL.add(
			panel,
			nameField.customEditor,
			1, row,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.HORIZONTAL,
			0, inset, 0, 0
		)

		EGBL.add(
			panel,
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
			panel,
			descField.customEditor,
			1, row,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.HORIZONTAL,
			rowDist, inset, 0, 0
		)

		if (supportImport) {
			EGBL.add(
				panel,
				JLabel("$importLabel:"),
				0, ++row,
				1, 1,
				0.0, 0.0,
				EGBL.NORTHWEST,
				EGBL.NONE,
				4 + rowDist, inset, 0, 0
			)

			EGBL.add(
				panel,
				importField,
				1, row,
				EGBL.REMAINDER, 1,
				0.0, 0.0,
				EGBL.WEST,
				EGBL.NONE,
				rowDist, inset, 0, 0
			)
		}

		if (supportOwnership) {
			EGBL.add(
				panel,
				JLabel("$ownedByMeLabel:"),
				0, ++row,
				1, 1,
				0.0, 0.0,
				EGBL.NORTHWEST,
				EGBL.NONE,
				4 + rowDist, inset, 0, 0
			)

			EGBL.add(
				panel,
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
				panel,
				JLabel("$visibilityLabel:"),
				0, ++row,
				1, 1,
				0.0, 0.0,
				EGBL.WEST,
				EGBL.NONE,
				rowDist, inset, 0, 0
			)

			val visibilityPanel = JPanel()
			visibilityPanel.layout = BoxLayout(visibilityPanel, BoxLayout.LINE_AXIS)
			visibilityPanel.add(visibilityField.customEditor)
			visibilityPanel.add(Box.createHorizontalStrut(5))
			visibilityPanel.add(visibilityDescLabel)

			visibilityDescLabel.border = null
			(visibilityField.customEditor as JComboBox<*>).addItemListener {
				visibilityDescLabel.text = (visibilityField.value as LibraryVisibility).description()
			}

			EGBL.add(
				panel,
				visibilityPanel,
				1, row,
				EGBL.REMAINDER, 1,
				0.0, 0.0,
				EGBL.WEST,
				EGBL.NONE,
				rowDist, inset, 0, 0
			)
		}

		// Filler

		val filler = JPanel()
		EGBL.add(
			panel,
			filler,
			10, ++row,
			EGBL.REMAINDER, EGBL.REMAINDER,
			1.0, 1.0,
			EGBL.NORTHWEST,
			EGBL.BOTH
		)

		return panel
	}

	private fun confirmOwnByMe(): Boolean =
		JOptionPane.showConfirmDialog(
			parent,
			Translations.getString("library.action.ownByMe.text"),
			Translations.getString("library.action.ownByMe.name"),
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.WARNING_MESSAGE
		) == JOptionPane.OK_OPTION

	private class MyMessageDisplay : JLabel(" ", LEFT), PreferencesMessageDisplay {

		init {
			foreground = UiUtil.errorTextColor
		}

		override fun showMessage(message: String) {
			if (StringUtils.isEmpty(message)) {
				hideMessage()
			} else {
				updateMessage(message)
			}
		}

		override fun hideMessage() {
			updateMessage(" ")
		}

		private fun updateMessage(message: String) {
			this.text = message
		}
	}
}