package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.*

/**
 * A [Theme] is a collection of [Style]s and other graphical properties that define the look of
 * graphical objects. An application using the `draw` framework has a collection of [Theme]s
 * from which the user can choose one to be the current one.
 */
interface Theme {

	/** The displayable name of this [Theme]. This name is not translated to the user's language.*/
	val name: String

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
	private const val PROP_THEME = "ch.scorpion.antares.view.theme"

	private val themes = mutableListOf<Theme>(DrawTheme())

	private var uiTheme: Theme = themes[0]
		set(value) {
			if (field != value) {
				field = value
				field.activateIn(uiStyleProvider as StyleRepository, styleOnly = true)
			}
		}

	var current: Theme = themes[0]
		private set(value) {
			field = value
			field.activateIn(DrawStyleModule.styleProvider, styleOnly = false)
			uiTheme = determineUITheme()
		}

	/** The [StyleProvider] that provides the [Style]s to be used for displaying over white UI background.*/
	val uiStyleProvider: StyleProvider = StyleRepository()

	fun setCurrent(name: String) {
		current = get(name) ?: throw NoSuchElementException("No theme with name '$name' defined")
		BaseModule.settings.set(PROP_THEME, name)
		BaseModule.eventBus.post(ThemeEvent(current))
	}

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

			val storedThemeName = BaseModule.settings.getString(PROP_THEME, "")
			if (StringUtils.isNotEmpty(storedThemeName) && this.themes.firstOrNull { it.name == storedThemeName } != null) {
				setCurrent(storedThemeName)
			} else {
				current = this.themes[0]
			}
		}
	}

	fun allThemes(): Iterator<Theme> = themes.iterator()

}

data class ThemeEvent(val currentTheme: Theme)

open class DrawTheme(
	override val name: String = DEF_NAME,
	override val supportsWhiteBackground: Boolean = DEF_SUPPORTS_WHITE_BACKGROUND,
	private val referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider,
	private val referenceColors: List<CompositeColor> = DEF_REF_COLORS,
	private val predefinedColors: List<PredefinedColor> = DEF_PREDEFINED_COLORS,
	val background: Style = DEF_BACKGROUND,
	val figure: Style = DEF_FIGURE,
	val tooltip: Style = DEF_TOOLTIP,
	val shadow: CompositeColor = DEF_SHADOW
) : Theme {

	companion object {
		const val DEF_NAME = "default"
		const val DEF_SUPPORTS_WHITE_BACKGROUND = true
		val DEF_BACKGROUND = BasicStyle(CompositeColor(Color(240, 240, 240), Color.WHITE, Color.BLACK))
		val DEF_FIGURE = BasicStyle(CompositeColor(Color.BLACK, Color.WHITE, Color.BLACK))
		val DEF_TOOLTIP = BasicStyle(CompositeColor(foregroundColor = Color(249, 214, 54),
			backgroundColor = Color(255, 253, 219), textColor = Color.BLACK))
		val DEF_SHADOW = CompositeColor(Color.GRAY, Color.GRAY, Color.GRAY)
		const val REF_COLOR_ALPHA = 144
		val DEF_REF_COLORS = listOf(
			DrawGraphicsModule.RED.withAlpha(REF_COLOR_ALPHA),
			DrawGraphicsModule.BLUE.withAlpha(REF_COLOR_ALPHA),
			DrawGraphicsModule.GREEN.withAlpha(REF_COLOR_ALPHA),
			DrawGraphicsModule.YELLOW.withAlpha(REF_COLOR_ALPHA),
			DrawGraphicsModule.VIOLET.withAlpha(REF_COLOR_ALPHA),
			DrawGraphicsModule.PINK.withAlpha(REF_COLOR_ALPHA),
			DrawGraphicsModule.GRAY.withAlpha(REF_COLOR_ALPHA),
			DrawGraphicsModule.WHITE.withAlpha(REF_COLOR_ALPHA),
			DrawGraphicsModule.BLACK.withAlpha(REF_COLOR_ALPHA)
		)
		val DEF_PREDEFINED_COLORS = listOf(
			PredefinedColor(PredefinedColorIdentity.White, DrawGraphicsModule.WHITE),
			PredefinedColor(PredefinedColorIdentity.Black, DrawGraphicsModule.BLACK),
			PredefinedColor(PredefinedColorIdentity.Gray, DrawGraphicsModule.GRAY),
			PredefinedColor(PredefinedColorIdentity.Red, DrawGraphicsModule.RED),
			PredefinedColor(PredefinedColorIdentity.Blue, DrawGraphicsModule.BLUE),
			PredefinedColor(PredefinedColorIdentity.Green, DrawGraphicsModule.GREEN),
			PredefinedColor(PredefinedColorIdentity.Yellow, DrawGraphicsModule.YELLOW)
		)
	}

	override fun activateIn(styleRepository: StyleRepository, styleOnly: Boolean) {
		if (!styleOnly) {
			referenceColorSequenceProvider.replaceColors(referenceColors)
			predefinedColors.forEach { PredefinedColorRepository.register(it) }
		}
		styleRepository.registerStyle(StyleType.BACKGROUND, background)
		styleRepository.registerStyle(StyleType.FIGURE, figure)
		styleRepository.registerStyle(StyleType.TOOLTIP, tooltip)
	}
}
