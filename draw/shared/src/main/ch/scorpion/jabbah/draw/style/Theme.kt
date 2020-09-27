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
	 * Determines whether this [Theme] defines colors for objects that can be painted over white background.
	 * This is relevant when displaying individual figures directly on UI panels, such as trees or component
	 * preview panels.
	 */
	val supportsWhiteBackground: Boolean

	/**
	 * Notifies this [Theme] that is has become the current one in [Themes].
	 * Implementations should activateIn themselves by registering all their [Style]s
	 * with the [StyleRepository].
	 */
	fun activateIn(styleRepository: StyleRepository, styleOnly: Boolean)
}

/** Contains all available [Theme]s. */
object Themes {

	// TODO Bug: Wrong package name
	const val PROP_THEME = "ch.scorpion.antares.view.theme"

	private val themes = mutableListOf<Theme>(DrawTheme())

	private var uiTheme: Theme = themes[0]
		set(value) {
			if (field != value) {
				field = value
				field.activateIn(uiStyleProvider as StyleRepository, styleOnly = true)
			}
		}

	private var current: Theme = themes[0]
		set(value) {
			if (field !== value) {
				field = value
				field.activateIn(DrawStyleModule.styleProvider, styleOnly = false)
				uiTheme = determineUITheme()
			}
		}

	/** The [StyleProvider] that provides the [Style]s to be used for displaying over white UI background.*/
	val uiStyleProvider: StyleProvider = StyleRepository()

	fun <T : Theme> get(): T {
		@Suppress("UNCHECKED_CAST")
		return current as T
	}

	/** Returns the [Theme] suitable to be displayed over white UI background.*/
	fun <T : Theme> getUITheme(): T {
		@Suppress("UNCHECKED_CAST")
		return uiTheme as T
	}

	private fun determineUITheme(): Theme {
		if (current.supportsWhiteBackground) {
			return current
		}
		return themes.firstOrNull { it.supportsWhiteBackground } ?: current
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
				setCurrent(storedThemeName!!)
			} else {
				//current = this.themes.first()
				setCurrent(this.themes.first().name)
			}

			/*
			val storedThemeName = BaseModule.settings.getString(PROP_THEME, "")
			if (StringUtils.isNotEmpty(storedThemeName) && this.themes.firstOrNull { it.name == storedThemeName } != null) {
				setCurrent(storedThemeName)
			} else {
				current = this.themes[0]
			}
			 */
		}
	}


	private fun setCurrent(name: String) {
		current = get(name) ?: throw NoSuchElementException("No theme with name '$name' defined")
		//BaseModule.properties.customize(PROP_THEME, name)
		BaseModule.properties.set(PROP_THEME, name)
		BaseModule.eventBus.post(ThemeEvent(current))
	}

	fun allThemes(): Iterator<Theme> = themes.iterator()

}

data class ThemeEvent(val currentTheme: Theme)
