package io.antarescircuit.jabbah.app.find

import java.util.Locale
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

/** A live menu item, retaining its normal enabled state and checkbox behavior. */
internal data class MenuCommand(
	val item: JMenuItem,
	val menus: List<JMenu>,
) {
	val name: String get() = item.text.orEmpty()
	val path: String get() = menus.joinToString(" › ") { it.text.orEmpty() }
	val enabled: Boolean get() = item.isEnabled && item.isVisible && menus.all { it.isEnabled && it.isVisible }

	fun execute() {
		if (enabled) item.doClick(0)
	}
}

/** Searches the actual menu tree so context actions and recently opened files stay current. */
internal object MenuCommandSearch {
	fun collect(menuBar: JMenuBar): List<MenuCommand> = buildList {
		fun visit(menu: JMenu, parents: List<JMenu>) {
			if (!menu.isVisible) return
			val path = parents + menu
			for (index in 0 until menu.itemCount) {
				val item = menu.getItem(index) ?: continue
				if (!item.isVisible) continue
				if (item is JMenu) visit(item, path)
				else if (!item.text.isNullOrBlank()) add(MenuCommand(item, path))
			}
		}
		for (index in 0 until menuBar.menuCount) {
			menuBar.getMenu(index)?.let { visit(it, emptyList()) }
		}
	}

	fun filter(commands: List<MenuCommand>, query: String): List<MenuCommand> {
		val words = query.trim().lowercase(Locale.ROOT).split(Regex("\\s+")).filter { it.isNotEmpty() }
		if (words.isEmpty()) return commands
		return commands.mapNotNull { command ->
			val name = command.name.lowercase(Locale.ROOT)
			val path = command.path.lowercase(Locale.ROOT)
			val acronym = name.split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotEmpty() }.joinToString("") { it.first().toString() }
			if (!words.all { name.contains(it) || path.contains(it) || acronym.startsWith(it) }) return@mapNotNull null
			val score = when {
				name == words.joinToString(" ") -> 0
				name.startsWith(words.joinToString(" ")) -> 1
				words.all { name.contains(it) } -> 2
				words.size == 1 && acronym.startsWith(words[0]) -> 3
				else -> 4
			}
			command to score
		}.sortedBy { it.second }.map { it.first }
	}
}
