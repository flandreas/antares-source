package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.edit.auth.UserHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.ui.AbstractApplicationModeEditAction
import java.awt.BorderLayout
import java.awt.Frame
import javax.swing.*

class AddLibraryToDesktopAction(
	applicationModeHolder: ApplicationModeHolder
) : AbstractApplicationModeEditAction("library.selectionDialog.action", applicationModeHolder) {

	override fun calculateEnabledness(): Boolean = true

	override fun execute(event: ActionEvent) {
		LibrarySelectionPanel.showAsDialog(Frame.getFrames()[0])
	}
}

/**
 * Displays a list of all existing [Libraries][Library] and allows the user to open them.
 */
class LibrarySelectionPanel(
	private val managementService: LibraryManagementService = LibraryModule.libraryManagementService,
	libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	userHolder: UserHolder<User> = EditAuthModule.userHolder,
	private val closeHandler: () -> Unit
) : AbstractLibrarySelectionPanel(userHolder, isOpen = { it.uuid == libraryHolder.l?.uuid }) {

	companion object {
		private val LOG by logger(LibrarySelectionPanel::class)

		fun showAsDialog(parent: Frame) {
			DialogBuilder<LibrarySelectionPanel>(parent)
				.content { dialog -> LibrarySelectionPanel(closeHandler = { dialog.dispose() }) }
				.title(Translations.getString("library.selectionDialog.title"))
				.defaultButton { it.openButton }
				.nonResizable()
				.show()
		}
	}

	private val openAction = OpenAction()
	val openButton = createButton(openAction)

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
		buttonPanel.add(createButton(CancelAction()))
		buttonPanel.add(Box.createHorizontalStrut(2))
		buttonPanel.add(openButton)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	override fun currentLibraryIndex(): Int? = null

	override fun loadLibraryDirectoryEntries(): ListModel<LibraryDictionaryEntry> {
		val list = DefaultListModel<LibraryDictionaryEntry>()
		managementService.getLibraryDirectoryEntries().forEach {
			if (!isOpen(it)) {
				list.addElement(it)
			}
		}
		return list
	}

	override fun handleListDoubleClick(event: ActionEvent) {
		importSelectedLibrary()
	}

	override fun handleSelectionChanged() { }

	private fun importSelectedLibrary() {
		selectedLibrary?.let {
			InvocationHandler.invoke {
				managementService.addImport(it.uuid)
				closeHandler.invoke()
			}
		}
	}

	private inner class OpenAction : AbstractAction("library.dialog.open.action") {
		override fun execute(event: ActionEvent) {
			importSelectedLibrary()
		}
	}

	private inner class CancelAction : AbstractAction("library.dialog.cancel.action") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}
}