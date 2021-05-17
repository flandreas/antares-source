package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.UserHolder
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.ui.AbstractApplicationModeEditAction
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/** An [Action] that opens a dialog containing [LibraryPersistencePanel].*/
class ShowLibrariesDialogAction(private val parent: JFrame) : AbstractApplicationModeEditAction("library.dialog.action") {

	override fun execute(event: ActionEvent) {
		LibraryPersistencePanel.showAsDialog(parent)
	}

	override fun calculateEnabledness(): Boolean = true
}

/**
 * Displays a list of all existing [Libraries][Library] and allows the user to manage them.
 */
class LibraryPersistencePanel(
	private val service: LibraryManagementService = LibraryModule.libraryManagementService,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	private val userHolder: UserHolder = EditAuthModule.userHolder,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private val LOG by logger(LibraryPersistencePanel::class)

		fun showAsDialog(parent: JFrame) {
			DialogBuilder<LibraryPersistencePanel>(parent)
				.content { dialog -> LibraryPersistencePanel(closeHandler = { dialog.dispose() }) }
				.title(Translations.getString("library.dialog.title"))
				.defaultButton { it.openButton }
				.nonResizable()
				.show()
		}
	}

	private val libraryDictionaryEntries = JList(loadLibraryDirectoryEntries())
	private val descriptionTextArea = JTextArea()
	private val currentLibraryFont = libraryDictionaryEntries.font.deriveFont(Font.BOLD)
	private val openAction = OpenAction()
	private val deleteAction = DeleteAction()
	val openButton = createButton(openAction)

	private val selectedLibrary: LibraryDictionaryEntry? get() = libraryDictionaryEntries.selectedValue

	init {
		libraryDictionaryEntries.addListSelectionListener {
			updateDescription()
			updateActions()
		}
		libraryDictionaryEntries.addMouseListener(object: MouseAdapter() {
			override fun mouseClicked(e: MouseEvent?) {
				if (e!!.clickCount == 2) {
					openAction.execute(ActionWrapperSwing.toJabbahActionEvent(e))
				}
			}
		})
		buildUI()
		updateActions()

		libraryDictionaryEntries.requestFocusInWindow()
		currentLibraryIndex()?.let { libraryDictionaryEntries.selectedIndex = it }
	}

	private fun currentLibraryIndex(): Int? {
		if (libraryDictionaryEntries.model.size == 0) {
			return null
		}
		for (index in 0 until libraryDictionaryEntries.model.size) {
			if (libraryDictionaryEntries.model.getElementAt(index).uuid == libraryHolder.library.uuid) {
				return index
			}
		}
		return null
	}

	private fun buildUI() {
		layout = BorderLayout(0, 10)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		descriptionTextArea.lineWrap = true
		descriptionTextArea.wrapStyleWord = true
		descriptionTextArea.background = background
		descriptionTextArea.isEditable = false
		descriptionTextArea.rows = 6
		val descriptionScroll = UiUtil.decorateTextArea(descriptionTextArea)
		descriptionScroll.background = background

		val scrollPane = JScrollPane(libraryDictionaryEntries)
		scrollPane.preferredSize = Dimension(300, 300)
		add(scrollPane, BorderLayout.NORTH)

		add(descriptionScroll, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(openButton)
		buttonPanel.add(Box.createHorizontalStrut(2))
		buttonPanel.add(createButton(NewAction()))
		buttonPanel.add(Box.createHorizontalStrut(2))
		buttonPanel.add(createButton(deleteAction))
		buttonPanel.add(Box.createHorizontalStrut(9))

		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(createButton(CancelAction()))
		add(buttonPanel, BorderLayout.SOUTH)

		libraryDictionaryEntries.cellRenderer = LibraryListRenderer()
	}

	private fun createButton(action: Action): JButton {
		return JButton(ActionWrapperSwing(action))
	}

	private fun isReadonly(entry: LibraryDictionaryEntry): Boolean {
		return entry.author != userHolder.user.uuid
	}

	private fun loadLibraryDirectoryEntries(): ListModel<LibraryDictionaryEntry> {
		val list = DefaultListModel<LibraryDictionaryEntry>()
		service.getLibraryDirectoryEntries().forEach { list.addElement(it) }
		return list
	}

	private fun updateActions() {
		openAction.enabled =
			!libraryDictionaryEntries.isSelectionEmpty
			&& libraryDictionaryEntries.selectedValue.uuid != libraryHolder.l?.uuid

		deleteAction.enabled =
			!libraryDictionaryEntries.isSelectionEmpty
			&& libraryDictionaryEntries.selectedValue.uuid != libraryHolder.l?.uuid
			&& !isReadonly(libraryDictionaryEntries.selectedValue)
	}

	private fun updateDescription() {
		descriptionTextArea.text = selectedLibrary?.description?.value ?: ""
	}

	private inner class OpenAction : AbstractAction("library.dialog.open.action") {
		override fun execute(event: ActionEvent) {
			selectedLibrary?.let {
				LOG.debug("open library '${it.uuid}'")
				InvocationHandler.invoke {
					service.open(it.uuid)
					closeHandler.invoke()
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
			LOG.debug("new library")

			val parent = SwingUtilities.windowForComponent(this@LibraryPersistencePanel)
			var info: CreateLibraryPanel.CreateLibraryInfo

			while(true) {
				info = CreateLibraryPanel.showAsDialog(parent = parent, service = service) ?: return

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
				} else if (service.existsName(info.name)) {
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

			LOG.debug("creating new library '${info.name.getTranslation()}'")
			InvocationHandler.invoke {
				service.open(service.create(LibraryProperties(info.name), info.templateUuid))
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
					service.delete(it.uuid)
					closeHandler.invoke()
				}
			}
		}
	}

	private inner class LibraryListRenderer : DefaultListCellRenderer() {

		private val lockedIcon = UiUtil.themedIcon("/img/locked-16.png")
		private val unlockedIcon = UiUtil.themedIcon("/img/unlocked-16.png")

		override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
			val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
			val entry = value as LibraryDictionaryEntry
			renderer.font = if (entry.name == libraryHolder.library.name) currentLibraryFont else libraryDictionaryEntries.font
			renderer.icon = if (isReadonly(entry)) lockedIcon else unlockedIcon
			return renderer
		}
	}
}