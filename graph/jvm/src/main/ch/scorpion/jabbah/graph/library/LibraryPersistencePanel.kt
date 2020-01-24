package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.app.user.UserHolder
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.ui.AbstractApplicationModeEditAction
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
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
	private val userHolder: UserHolder = AppModule.userHolder,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private val LOG by logger(LibraryPersistencePanel::class)

		fun showAsDialog(parent: JFrame) {
			val dialog = JDialog(parent, true)
			BusyHandler.register(dialog, null)
			dialog.title = Translations.getString("library.dialog.title")
			dialog.contentPane.add(LibraryPersistencePanel(closeHandler = {
				dialog.isVisible = false
				dialog.dispose()
			}))
			dialog.isResizable = false
			dialog.pack()
			dialog.setLocationRelativeTo(parent)
			dialog.addWindowListener(object : WindowAdapter() {
				override fun windowClosed(e: WindowEvent?) {
					BusyHandler.deregister(dialog)
				}
			})
			dialog.isVisible = true
		}
	}

	private val libraryDictionaryEntries = JList(loadLibraryDirectoryEntries())
	private val descriptionTextArea = JTextArea()
	private val currentLibraryFont = libraryDictionaryEntries.font.deriveFont(Font.BOLD)
	private val openAction = OpenAction()
	private val deleteAction = DeleteAction()

	private val selectedLibrary: LibraryDictionaryEntry? get() = libraryDictionaryEntries.selectedValue

	init {
		libraryDictionaryEntries.addListSelectionListener {
			updateDescription()
			updateActions()
		}
		libraryDictionaryEntries.addMouseListener(object: MouseAdapter() {
			override fun mouseClicked(e: MouseEvent?) {
				if (e!!.clickCount == 2) {
					openAction.execute(ActionWrapperSwing.toJabbaActionEvent(e))
				}
			}
		})
		buildUI()
		updateActions()
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
		buttonPanel.add(JButton(ActionWrapperSwing(openAction)))
		buttonPanel.add(JButton(ActionWrapperSwing(NewAction())))
		buttonPanel.add(JButton(ActionWrapperSwing(deleteAction)))

		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(JButton(ActionWrapperSwing(CancelAction())))
		add(buttonPanel, BorderLayout.SOUTH)

		libraryDictionaryEntries.cellRenderer = LibraryListRenderer()
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


				if (StringUtils.isBlank(info.name.getTranslation())) {
					if (JOptionPane.showConfirmDialog(
						parent,
						Translations.getString("library.emptyName.msg"),
						Translations.getString("library.dialog.new.name.dialog.title"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
					) {
						return
					}
				} else if (service.exists(info.name)) {
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

				LOG.debug("delete library ${it.uuid}")
				InvocationHandler.invoke {
					service.delete(it.uuid)
					closeHandler.invoke()
				}
			}
		}
	}

	private inner class LibraryListRenderer : DefaultListCellRenderer() {

		private val lockedIcon = ImageIcon(LibraryPersistencePanel::class.java.getResource("/img/locked-16.png"))
		private val unlockedIcon = ImageIcon(LibraryPersistencePanel::class.java.getResource("/img/unlocked-16.png"))

		override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
			val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
			val entry = value as LibraryDictionaryEntry
			renderer.font = if (entry.name == libraryHolder.library.name) currentLibraryFont else libraryDictionaryEntries.font
			renderer.icon = if (isReadonly(entry)) lockedIcon else unlockedIcon
			return renderer
		}
	}
}