package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.module.BaseModule

/**
 * A [Theme] is a collection of [Style]s and other graphical properties that define the look of
 * graphical objects. An application using the `draw` framework has a collection of [Theme]s
 * from which the user can choose one to be the current one.
 */
interface Theme {

	/** The displayable name of this [Theme]. This name is not translated to the user's language.*/
	val name: String

	/** Determines whether this [Theme] requires a dark UI look & feel.*/
	val dark: Boolean

	/**
	 * Notifies this [Theme] that is has become the current one in [Themes].
	 * Implementations should activateIn themselves by registering all their [Style]s
	 * with the [StyleRepository].
	 */
	fun activateIn(styleRepository: StyleRepository, styleOnly: Boolean)
}

/** Contains all available [Theme]s. */
object Themes {

	const val PROP_THEME = "draw.style.Theme.name"

	private val themes = mutableListOf<Theme>(DrawTheme())

	private var current: Theme = themes[0]
		set(value) {
			if (field !== value) {
				field = value
				field.activateIn(DrawStyleModule.styleProvider, styleOnly = false)
				BaseModule.eventBus.post(ThemeEvent(current))
			}
		}

	fun <T : Theme> get(): T {
		@Suppress("UNCHECKED_CAST")
		return current as T
	}

	fun get(name: String): Theme? {
		return themes.firstOrNull { it.name == name }
	}

	fun exists(name: String): Boolean {
		return get(name) != null
	}

	/** Registers the specified [Theme]s by replacing all existing [Theme]s.*/
	fun register(vararg themes: Theme) {
		if (themes.isNotEmpty()) {
			this.themes.clear()
			this.themes.addAll(themes)

			val storedThemeName = if (BaseModule.properties.contains(PROP_THEME)) {
				BaseModule.properties.getString(PROP_THEME)
			} else {
				null
			}

			if (this.themes.map { it.name }.contains(storedThemeName)) {
				current = get(storedThemeName!!)!!
			} else {
				current = get(this.themes.first().name)!!
				store(current.name)
			}
		}
	}

	fun allThemes(): Iterator<Theme> = themes.iterator()

	fun store(themeName: String) {
		BaseModule.properties.set(PROP_THEME, themeName)
	}

}

data class ThemeEvent(val currentTheme: Theme)
