package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.edit.auth.UserHolder
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * An abstract [AbstractLibraryPersistencePanel] that displays a list of all existing [Libraries][Library]
 * and the description of the selected [Library].
 */
abstract class AbstractLibrarySelectionPanel(
	private val userHolder: UserHolder<User> = EditAuthModule.userHolder,
	protected val isOpen: (entry: LibraryDictionaryEntry) -> Boolean
) : JPanel() {

	private val libraryDictionaryEntries = JList<LibraryDictionaryEntry>()
	private val descriptionTextArea = JTextArea()

	private var entries = listOf<LibraryDictionaryEntry>()

	val selectedLibrary: LibraryDictionaryEntry? get() = libraryDictionaryEntries.selectedValue

	init {
		libraryDictionaryEntries.addListSelectionListener {
			updateDescription()
			handleSelectionChanged()
		}
		libraryDictionaryEntries.addMouseListener(object: MouseAdapter() {
			override fun mouseClicked(e: MouseEvent?) {
				if (e!!.clickCount == 2) {
					handleListDoubleClick(ActionWrapperSwing.toJabbahActionEvent(e))
				}
			}
		})
	}

	fun selectLibrary(uuid: UUID) {
		getLibraryIndex(uuid)?.let { libraryDictionaryEntries.selectedIndex = it }
	}

	protected fun load() {
		entries = loadLibraryDirectoryEntries()
		val model = DefaultListModel<LibraryDictionaryEntry>()
		model.addAll(entries)
		libraryDictionaryEntries.model = model
	}

	protected fun selectCurrentLibrary(library: Library? = null) {
		SwingUtilities.invokeLater {
			libraryDictionaryEntries.requestFocusInWindow()
			library?.let { lib ->
				getLibraryIndex(lib.uuid)?.let { index ->
					libraryDictionaryEntries.selectedIndex = index
					libraryDictionaryEntries.ensureIndexIsVisible(index)
				}
			}
		}
	}

	protected fun selectFirstLibrary() {
		libraryDictionaryEntries.requestFocusInWindow()
		if (libraryDictionaryEntries.model.size > 0) {
			libraryDictionaryEntries.selectedIndex = 0
		}
	}

	protected open fun buildUI() {
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

		libraryDictionaryEntries.cellRenderer = LibraryListRenderer(
			normalFont = libraryDictionaryEntries.font,
			isOpen = isOpen,
			isReadOnly = ::isReadonly,
			displayIcon = ::needsLockIcon
		)
	}

	protected abstract fun loadLibraryDirectoryEntries(): List<LibraryDictionaryEntry>

	protected abstract fun handleListDoubleClick(event: ActionEvent)

	protected abstract fun handleSelectionChanged()

	protected abstract fun currentLibraryIndex(): Int?

	private fun isReadonly(entry: LibraryDictionaryEntry): Boolean =
		entry.author != userHolder.user.identity

	protected fun getLibraryIndex(uuid: UUID): Int? {
		if (libraryDictionaryEntries.model.size == 0) {
			return null
		}
		for (index in 0 until libraryDictionaryEntries.model.size) {
			if (libraryDictionaryEntries.model.getElementAt(index).uuid == uuid) {
				return index
			}
		}
		return null
	}

	private fun needsLockIcon(): Boolean = entries.any { isReadonly(it) }

	private fun updateDescription() {
		descriptionTextArea.text = selectedLibrary?.description?.value ?: ""
	}

	protected fun createButton(action: Action): JButton = JButton(ActionWrapperSwing(action))
}