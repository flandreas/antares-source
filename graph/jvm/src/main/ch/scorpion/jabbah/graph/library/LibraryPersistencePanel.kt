package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

/** An [Action] that opens a dialog containing [LibraryPersistencePanel].*/
class ShowLibrariesDialogAction(private val parent: JFrame) : AbstractAction("library.dialog.action") {

	override fun execute(event: ActionEvent) {
		LibraryPersistencePanel.showAsDialog(parent)
	}
}

/**
 * Displays a list of all existing [Libraries][Library] and allows the user to manage them.
 */
class LibraryPersistencePanel(
	private val service: LibraryManagementService = LibraryModule.libraryManagementService,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
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

	private val libraryNameList = JList<String>(loadLibraryNames())
	private val currentLibraryFont = libraryNameList.font.deriveFont(Font.BOLD)
	private val openAction = OpenAction()
	private val selectedLibraryName: String? get() = libraryNameList.selectedValue

	init {
		libraryNameList.addListSelectionListener { updateActions() }
		libraryNameList.addMouseListener(object: MouseAdapter() {
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
		layout = BorderLayout()
		preferredSize = Dimension(400, 500)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		val scrollPane = JScrollPane(libraryNameList)
		add(scrollPane, BorderLayout.CENTER)

		val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
		buttonPanel.add(JButton(ActionWrapperSwing(openAction)))
		buttonPanel.add(JButton(ActionWrapperSwing(NewAction())))
		buttonPanel.add(JButton(ActionWrapperSwing(CancelAction())))
		add(buttonPanel, BorderLayout.SOUTH)

		libraryNameList.cellRenderer = LibraryListRenderer()
	}

	private fun loadLibraryNames(): ListModel<String> {
		val list = DefaultListModel<String>()
		service.getLibraryNames().forEach { list.addElement(it) }
		return list
	}

	private fun updateActions() {
		openAction.enabled = !libraryNameList.isSelectionEmpty && libraryNameList.selectedValue != libraryHolder.l?.name
	}

	private inner class OpenAction : AbstractAction("library.dialog.open.action") {
		override fun execute(event: ActionEvent) {
			LOG.debug("LibraryPersistencePanel: open library '$selectedLibraryName'")
			InvocationHandler.invoke {
				service.open(selectedLibraryName!!)
				closeHandler.invoke()
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
			LOG.debug("LibraryPersistencePanel: new library")

			var info: CreateLibraryPanel.CreateLibraryInfo
			while(true) {
				info = CreateLibraryPanel.showAsDialog(service = service)
					?: return

				if (StringUtils.isBlank(info.libraryName)) {
					if (JOptionPane.showConfirmDialog(
						this@LibraryPersistencePanel,
						Translations.getString("library.dialog.new.emptyName.msg"),
						Translations.getString("library.dialog.new.name.dialog.title"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
					) {
						return
					}
				} else if (service.exists(info.libraryName)) {
					if (JOptionPane.showConfirmDialog(
						this@LibraryPersistencePanel,
						Translations.getString("library.dialog.new.duplicate.msg", info.libraryName),
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

			LOG.debug("LibraryPersistencePanel: creating new library '${info.libraryName}'")
			InvocationHandler.invoke {
				service.open(service.create(info.libraryName, info.templateName))
				closeHandler.invoke()
			}
		}
	}

	private inner class LibraryListRenderer : DefaultListCellRenderer() {
		override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
			val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
			val name = value as String
			if (name == libraryHolder.library.name) {
				renderer.font = currentLibraryFont
			} else {
				renderer.font = libraryNameList.font
			}
			return renderer
		}
	}
}