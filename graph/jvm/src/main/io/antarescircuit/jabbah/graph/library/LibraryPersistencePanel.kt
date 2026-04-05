package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.edit.auth.*
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import io.antarescircuit.jabbah.graph.ui.AbstractApplicationModeEditAction
import java.awt.BorderLayout
import java.awt.Frame
import javax.swing.*

/** An [Action] that opens a dialog containing [LibraryPersistencePanel].*/
class ShowLibrariesDialogAction(
	applicationModeHolder: ApplicationModeHolder,
	private val parent: JFrame
) : AbstractApplicationModeEditAction("library.dialog.action", applicationModeHolder) {

	companion object {
		private val LOG by logger(ShowLibrariesDialogAction::class)
	}

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		try {
			LibraryPersistencePanel.showAsDialog(name, parent)
		}  catch (x: Exception) {
			LOG.error("Error while reading library files", x)
			JOptionPane.showMessageDialog(
				Frame.getFrames()[0],
				Translations.getString("base.readFile.ioError.msg", x.message ?: ""),
				name,
				JOptionPane.ERROR_MESSAGE
			)
		}
	}

	override fun calculateEnabled(): Boolean = true
}

/**
 * Displays a list of all existing [Libraries][Library] and allows the user to manage them.
 */
class LibraryPersistencePanel(
	private val managementService: LibraryManagementService = LibraryModule.libraryManagementService,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	userHolder: UserHolder<User> = EditAuthModule.userHolder,
	private val closeHandler: () -> Unit
) : AbstractLibraryPersistencePanel(managementService, userHolder, isOpen = { it.uuid == libraryHolder.l?.uuid }, "library") {

	companion object {

		private val LOG by logger(LibraryPersistencePanel::class)

		fun showAsDialog(title: String, parent: JFrame) {
			DialogBuilder<LibraryPersistencePanel>(parent)
				.content { dialog -> LibraryPersistencePanel(closeHandler = { dialog.dispose() }) }
				.title(title)
				.defaultButton { it.openButton }
				.nonResizable()
				.show()
		}
	}

	private val openAction = OpenAction()

	private val deleteAction = DeleteAction()

	val openButton = createButton(openAction)

	init {
		buildUI()
		load()
		selectCurrentLibrary(libraryHolder.l)
	}

	/** ---- [AbstractLibrarySelectionPanel] */

	override val importProcess: AbstractLibraryImportProcess =
		LibraryImportProcess(
			managementService,
			userHolder,
			this,
			Translations.getString("$importActionNameKey.name"),
			::successHandler
		)

	override fun buildUI() {
		super.buildUI()

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(openButton)
		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		buttonPanel.add(createButton(NewAction()))
		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		buttonPanel.add(createButton(deleteAction))
		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GROUP_GAP))
		buttonPanel.add(createButton(exportAction))
		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		buttonPanel.add(createButton(importAction))
		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GROUP_GAP))

		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(createButton(CancelAction()))
		add(buttonPanel, BorderLayout.SOUTH)
	}

	override val fileExtension: String get() = AbstractLibraryImportProcess.libraryFileExtension

	override val fileTypeName: String get() = AbstractLibraryImportProcess.libraryFileTypeName

	override fun loadLibraryDirectoryEntries(): List<LibraryDictionaryEntry> =
		managementService.getLibraryDirectoryEntries()
			.filter { Authorizer.isCurrentUserAuthorizedTo(Operation.View, it) }

	override fun handleListDoubleClick(event: ActionEvent) {
		openAction.execute(event)
	}

	override fun getExportSuccessMsg(entry: LibraryDictionaryEntry): String =
		Translations.getString("library.dialog.export.success.msg", entry.name.value)

	override val exportActionNameKey: String get() = "library.dialog.export.action"

	override val importActionNameKey: String get() = "library.dialog.import.action"

	override val fileExtensionFilterName: String
		get() = Translations.getString("library.dialog.import.filter.name")

	override fun currentLibraryIndex(): Int? = getLibraryIndex(libraryHolder.library.uuid)

	override fun handleSelectionChanged() {
		openAction.enabled = selectedLibrary?.uuid != libraryHolder.l?.uuid
		deleteAction.enabled = selectedLibrary?.let { it.uuid != libraryHolder.l?.uuid } ?: false
		exportAction.enabled = selectedLibrary != null
	}

	/** ---- [LibraryPersistencePanel] */

	private inner class OpenAction : AbstractAction("library.dialog.open.action") {
		override fun execute(event: ActionEvent) {
			selectedLibrary?.let {
				InvocationHandler.invoke {
					try {
						managementService.open(getLibraryIdentity(it.uuid))
						closeHandler.invoke()
					} catch (e: Throwable) {
						LOG.error("Error when opening library '${it.uuid}'", e)
						JOptionPane.showMessageDialog(
							this@LibraryPersistencePanel,
							Translations.getString("library.dialog.open.error", e.message ?: "Unknown"),
							this.name,
							JOptionPane.ERROR_MESSAGE)
					}
				}
			}
		}
	}

	private inner class CancelAction : AbstractAction("library.dialog.cancel.action") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}

	private inner class NewAction : AbstractAction("library.dialog.new.action") {

		override fun execute(event: ActionEvent) {
			LOG.userTrail("new library")

			val parent = SwingUtilities.windowForComponent(this@LibraryPersistencePanel)
			var info: CreateLibraryInfo

			while(true) {
				info = CreateLibraryPanel.showAsDialog(parent = parent, service = managementService) ?: return

				if (info.name.isEmpty) {
					if (JOptionPane.showConfirmDialog(
						parent,
						Translations.getString("library.emptyName.msg"),
						Translations.getString("library.dialog.new.name.dialog.title"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
					) {
						return
					}
				} else if (managementService.existsName(info.name)) {
					if (JOptionPane.showConfirmDialog(
						parent,
						Translations.getString("library.duplicate.msg", info.name.getTranslation()),
						Translations.getString("library.dialog.new.name.dialog.title"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
					) {
						return
					}
				} else {
					break
				}
			}

			LOG.userTrail("creating new library '${info.name.getTranslation()}'")
			InvocationHandler.invoke {
				managementService.open(
					managementService.create(LibraryProperties(info.name),
					info.importUuid?.let { getLibraryIdentity(it) }),
				)
				closeHandler.invoke()
			}
		}
	}

	private inner class DeleteAction : AbstractAction("library.dialog.delete.action") {

		override fun execute(event: ActionEvent) {
			selectedLibrary?.let {
				if (JOptionPane.showConfirmDialog(
					SwingUtilities.windowForComponent(this@LibraryPersistencePanel),
					Translations.getString("library.dialog.delete.confirmation.msg", it.name),
					Translations.getString("library.dialog.delete.title"),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION
				) {
					return
				}

				LOG.trace("delete library ${it.uuid}")
				InvocationHandler.invoke {
					managementService.delete(getLibraryIdentity(it.uuid))
					load()
				}
			}
		}
	}
}