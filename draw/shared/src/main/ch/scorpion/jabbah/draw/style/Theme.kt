package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor

interface Theme {
    val name: String

    /**
     * Notifies this [Theme] that is has become the current one in [Themes].
     * Implementations should activate themselves by registering all their [Style]s
     * with [StyleRepository].
     */
    fun activate()
}

object Themes {

    private val themes = mutableListOf<Theme>(DrawTheme())

    private var _current: Theme = themes[0]
    set(value) {
        field = value
        field.activate()
        BaseModule.eventBus.post(ThemeEvent(_current))
    }

    val current: Theme get() = _current

    fun setCurrent(name: String) {
        _current = get(name) ?: throw NoSuchElementException("No theme with name '$name' defined")
    }

    fun <T: Theme> get(): T {
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
        if (themes != null && themes.size > 0) {
            this.themes.clear()
            this.themes.addAll(themes)
            _current = this.themes[0]
        }
    }

    fun allThemes(): Iterator<Theme> = themes.iterator()
}

data class ThemeEvent(val currentTheme: Theme)

open class DrawTheme(
    override val name: String = DEF_NAME,
    protected val styleRepository: StyleRepository = DrawStyleModule.styleProvider,
    val background: Style = DEF_BACKGROUND,
    val figure: Style = DEF_FIGURE
) : Theme {

    companion object {
        val DEF_NAME = "default"
        val DEF_BACKGROUND = BasicStyle(CompositeColor(Color.BLACK, Color.WHITE, Color.BLACK))
        val DEF_FIGURE = BasicStyle(CompositeColor(Color.BLACK, Color.WHITE, Color.BLACK))
    }

    override fun activate() {
        styleRepository.registerStyle(StyleType.BACKGROUND, background)
        styleRepository.registerStyle(StyleType.FIGURE, figure)
    }
}
