package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import java.awt.Component
import java.awt.Font
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList

class LibraryListRenderer(
	private val normalFont: Font,
	private val isOpen: (entry: LibraryDictionaryEntry) -> Boolean,
	private val isReadOnly: (entry: LibraryDictionaryEntry) -> Boolean,
	private val displayIcon: () -> Boolean = { true }
) : DefaultListCellRenderer() {

	companion object {
		private val lockedIcon = UiUtil.themedIcon("/img/locked-16.png")
		private val unlockedIcon = UiUtil.themedIcon("/img/unlocked-16.png")
		private val emptyIcon = UiUtil.themedIcon("/img/empty-16.png")
	}

	private val openLibraryFont: Font = normalFont.deriveFont(Font.BOLD)

	override fun getListCellRendererComponent(
		list: JList<*>?,
		value: Any?,
		index: Int,
		isSelected: Boolean,
		cellHasFocus: Boolean
	): Component {
		val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
		val entry = value as LibraryDictionaryEntry

		renderer.font = if (isOpen(entry)) openLibraryFont else normalFont
		renderer.icon = if (isReadOnly(entry)) lockedIcon else if (displayIcon()) emptyIcon else null

		return renderer
	}
}