package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.edit.auth.*
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import java.awt.BorderLayout
import java.awt.Frame
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * Displays a list of all existing [Libraries][Library] and allows the user to select one of them.
 */
class LibrarySelectionPanel(
	openActionNameBaseKey: String,
	private val managementService: LibraryManagementService = LibraryModule.libraryManagementService,
	libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	userHolder: UserHolder<User> = EditAuthModule.userHolder,
	private val closeHandler: () -> Unit
) : AbstractLibrarySelectionPanel(userHolder, isOpen = { libraryHolder.library.expandedImports.libraries.map { it.uuid }.contains(it.uuid) }) {

	companion object {

		fun showAsDialog(parent: Frame, openActionNameBaseKey: String = "library.dialog.open.action", title: String): UUID? {
			val builder = DialogBuilder<LibrarySelectionPanel>(parent)
				.content { dialog -> LibrarySelectionPanel(openActionNameBaseKey, closeHandler = { dialog.dispose() }) }
				.title(title)
				.defaultButton { it.openButton }
				.nonResizable()
				.show()

			return builder.content.result
		}
	}

	private val openAction = OpenAction(openActionNameBaseKey)
	val openButton = createButton(openAction)

	/** Contains the [UUID] of the selected [Library] after the user has closed the dialog.*/
	var result: UUID? = null
		private set

	init {
		buildUI()
		load()
		selectFirstLibrary()
	}

	override fun buildUI() {
		super.buildUI()

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(buttonPanel, openButton, createButton(CancelAction()))

		add(buttonPanel, BorderLayout.SOUTH)
	}

	override fun currentLibraryIndex(): Int? = null

	override fun loadLibraryDirectoryEntries(): List<LibraryDictionaryEntry> =
		managementService.getLibraryDirectoryEntries()
			.filter { !isOpen(it) }
			.filter { Authorizer.isCurrentUserAuthorizedTo(Operation.View, it) }

	override fun handleListDoubleClick(event: ActionEvent) {
		result = selectedLibrary?.identification?.uuid
		closeHandler.invoke()
	}

	override fun handleSelectionChanged() { }

	private inner class OpenAction(baseNameKey: String) : AbstractAction(baseNameKey) {
		override fun execute(event: ActionEvent) {
			result = selectedLibrary?.identification?.uuid
			closeHandler.invoke()
		}
	}

	private inner class CancelAction : AbstractAction("library.dialog.cancel.action") {
		override fun execute(event: ActionEvent) {
			result = null
			closeHandler.invoke()
		}
	}
}