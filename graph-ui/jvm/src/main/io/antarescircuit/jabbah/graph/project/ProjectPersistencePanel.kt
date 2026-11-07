package io.antarescircuit.jabbah.graph.project

import io.antarescircuit.jabbah.app.ApplicationTooOldException
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.auth.User
import io.antarescircuit.jabbah.edit.auth.UserHolder
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import io.antarescircuit.jabbah.graph.ui.AbstractApplicationModeEditAction
import java.awt.BorderLayout
import java.awt.Frame
import javax.swing.*

/** Opens and shows the [ProjectPersistencePanel] in a modal dialog.*/
class ShowProjectsDialogAction(
	applicationModeHolder: ApplicationModeHolder,
	private val parent: JFrame
) : AbstractApplicationModeEditAction("project.dialog.action", applicationModeHolder) {

	companion object {
		private val LOG by logger(ShowProjectsDialogAction::class)
	}

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		try {
			ProjectPersistencePanel.showAsDialog(parent)
		} catch (x: Exception) {
			LOG.error("Error while reading project files", x)
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
 * Displays a list of all existing project names and allows the user to open a project.
 */
class ProjectPersistencePanel(
	private val managementService: ProjectManagementService = ProjectModule.projectManagementService,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	userHolder: UserHolder<User> = EditAuthModule.userHolder,
	private val closeHandler: () -> Unit
) : AbstractLibraryPersistencePanel(managementService, userHolder, isOpen = { it.uuid == libraryHolder.l?.uuid }, "project") {

	companion object {

		private val LOG by logger(ProjectPersistencePanel::class)

		fun showAsDialog(parent: JFrame) {
			DialogBuilder<ProjectPersistencePanel>(parent)
				.content { dialog -> ProjectPersistencePanel(closeHandler = { dialog.dispose() }) }
				.title(Translations.getString("project.dialog.action.name"))
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
		ProjectImportProcess(
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
		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GROUP_GAP))
		buttonPanel.add(createButton(CancelAction()))

		add(buttonPanel, BorderLayout.SOUTH)
	}

	override val fileExtension: String get() = AbstractLibraryImportProcess.projectFileExtension

	override val fileTypeName: String get() = AbstractLibraryImportProcess.projectFileTypeName

	override fun loadLibraryDirectoryEntries(): List<LibraryDictionaryEntry> =
		managementService
			.getProjectDirectoryEntries()
			.sortedBy { it.name.value }

	override fun handleListDoubleClick(event: ActionEvent) {
		openAction.execute(event)
	}

	override fun currentLibraryIndex(): Int? =
		libraryHolder.l?.uuid?.let {
			getLibraryIndex(it)
		}

	override fun getExportSuccessMsg(entry: LibraryDictionaryEntry): String =
		Translations.getString("project.dialog.export.success.msg", entry.name.value)

	override val exportActionNameKey: String get() = "project.dialog.export.action"

	override val importActionNameKey: String get() = "project.dialog.import.action"

	override val fileExtensionFilterName: String
		get() = Translations.getString("project.dialog.import.filter.name")

	override fun handleSelectionChanged() {
		openAction.enabled = selectedLibrary?.uuid != libraryHolder.l?.uuid
		deleteAction.enabled = selectedLibrary != null
		exportAction.enabled = selectedLibrary != null
		importAction.enabled = true
	}

	/** ---- [ProjectPersistencePanel] */

	private inner class OpenAction : AbstractAction("project.dialog.open.action") {
		override fun execute(event: ActionEvent) {
			selectedLibrary?.let {
				InvocationHandler.invoke {
					try {
						managementService.open(getLibraryIdentity(it.uuid))
						closeHandler.invoke()
					} catch (e: ApplicationTooOldException) {
						JOptionPane.showConfirmDialog(
							this@ProjectPersistencePanel,
							e.message,
							name,
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE
						)
					} catch (e: Throwable) {
						LOG.error("Error when opening project '${it.uuid}'", e)
						JOptionPane.showMessageDialog(
							Frame.getFrames()[0],
							Translations.getString("base.readFile.ioError.msg", e.message ?: ""),
							name,
							JOptionPane.ERROR_MESSAGE
						)
					}
				}
			}
		}
	}

	private inner class CancelAction : AbstractAction("project.dialog.cancel.action") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}

	private inner class NewAction : AbstractAction("project.dialog.new.action") {
		override fun execute(event: ActionEvent) {
			LOG.trace("create new project")
			var properties: LibraryProperties?
			while (true) {
				properties = LibraryPropertiesPanel.showAsDialog(
					supportOwnership = false,
					supportImport = true,
					isSystem = false,
					parent = this@ProjectPersistencePanel,
					title = Translations.getString("project.dialog.new.dialog.title"))
				if (properties == null) {
					return
				}
				if (properties.name.isEmpty || StringUtils.isBlank(properties.name.getTranslation())) {
					if (JOptionPane.showConfirmDialog(
							this@ProjectPersistencePanel,
							Translations.getString("project.emptyName.msg"),
							Translations.getString("project.dialog.new.dialog.title"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
					) {
						return
					}
				} else if (managementService.existsName(properties.name)) {
					if (JOptionPane.showConfirmDialog(
							this@ProjectPersistencePanel,
							Translations.getString("project.duplicate.msg", properties.name.getTranslation()),
							Translations.getString("project.dialog.new.dialog.title"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
					) {
						return
					}
				} else {
					break
				}
			}

			LOG.userTrail("Create new project '${properties.name.getTranslation()}'")
			val project = managementService.create(properties)
			LOG.debug("Created new project ${project.uuid}")

			managementService.open(project)
			closeHandler.invoke()
		}
	}

	private inner class DeleteAction : AbstractAction("project.dialog.delete.action") {
		override fun execute(event: ActionEvent) {
			selectedLibrary?.let {
				if (JOptionPane.showConfirmDialog(
						this@ProjectPersistencePanel,
						Translations.getString("project.dialog.delete.confirm.msg", it.name.value),
						Translations.getString("project.dialog.delete.action.name"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION
				) {
					LOG.userTrail("Delete project '${it.uuid}'")
					managementService.delete(getLibraryIdentity(it.uuid))
					load()
				}
			}
		}
	}
}