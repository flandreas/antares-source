package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider

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

object Themes {

    private const val PROP_THEME = "ch.scorpion.antares.view.theme"
    private val themes = mutableListOf<Theme>(DrawTheme())

    private var _current: Theme = themes[0]
        set(value) {
            field = value
            field.activateIn(DrawStyleModule.styleProvider, styleOnly = false)
            _uiTheme = determineUITheme()
        }

    private var _uiTheme: Theme = themes[0]
        set(value) {
            if (field != value) {
                field = value
                field.activateIn(uiStyleProvider as StyleRepository, styleOnly = true)
            }
        }

    val current: Theme get() = _current

    /** The [StyleProvider] that provides the [Style]s to be used for displaying over white UI background.*/
    val uiStyleProvider: StyleProvider = StyleRepository()

    fun setCurrent(name: String) {
        _current = get(name) ?: throw NoSuchElementException("No theme with name '$name' defined")
        BaseModule.settings.set(PROP_THEME, name)
        BaseModule.eventBus.post(ThemeEvent(_current))
    }

    fun <T: Theme> get(): T {
        return current as T
    }

    /** Returns the [Theme] suitable to be displayed over white UI background.*/
    fun <T: Theme> getUITheme(): T {
        return _uiTheme as T
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
        if (themes != null && themes.isNotEmpty()) {
            this.themes.clear()
            this.themes.addAll(themes)

            val storedThemeName = BaseModule.settings.getString(PROP_THEME, "")
            if (StringUtils.isNotEmpty(storedThemeName) && this.themes.firstOrNull { it.name == storedThemeName } != null) {
                setCurrent(storedThemeName)
            } else {
                _current = this.themes[0]
            }
        }
    }

    fun allThemes(): Iterator<Theme> = themes.iterator()

}

data class ThemeEvent(val currentTheme: Theme)

open class DrawTheme(
        override val name: String = DEF_NAME,
        override val supportsWhiteBackground: Boolean = DEF_SUPPORTS_WHITE_BACKGROUND,
        protected val referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider,
        val referenceColors: List<CompositeColor> = DEF_REF_COLORS,
        val background: Style = DEF_BACKGROUND,
        val figure: Style = DEF_FIGURE
) : Theme {

    companion object {
        val DEF_NAME = "default"
        val DEF_SUPPORTS_WHITE_BACKGROUND = true
        val DEF_BACKGROUND = BasicStyle(CompositeColor(Color.BLACK, Color.WHITE, Color.BLACK))
        val DEF_FIGURE = BasicStyle(CompositeColor(Color.BLACK, Color.WHITE, Color.BLACK))
        val DEF_REF_COLORS = listOf<CompositeColor>(
                DrawGraphicsModule.RED,
                DrawGraphicsModule.BLUE,
                DrawGraphicsModule.GREEN,
                DrawGraphicsModule.YELLOW,
                DrawGraphicsModule.VIOLET,
                DrawGraphicsModule.PINK,
                DrawGraphicsModule.GRAY,
                DrawGraphicsModule.WHITE,
                DrawGraphicsModule.BLACK
        )
    }

    override fun activateIn(styleRepository: StyleRepository, styleOnly: Boolean) {
        if (!styleOnly) {
            referenceColorSequenceProvider.replaceColors(referenceColors)
        }
        styleRepository.registerStyle(StyleType.BACKGROUND, background)
        styleRepository.registerStyle(StyleType.FIGURE, figure)
    }
}
