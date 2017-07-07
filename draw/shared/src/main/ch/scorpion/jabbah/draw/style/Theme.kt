package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor

interface Theme {
    val name: String
}

object Themes {

    private val themes = mutableListOf<Theme>(DrawTheme())

    private var _current: Theme = themes[0]

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
}

open class DrawTheme(
    override val name: String = DEF_NAME,
    val background: Style = DEF_BACKGROUND
) : Theme {

    companion object {
        val DEF_NAME = "default"
        val DEF_BACKGROUND = BasicStyle(CompositeColor(Color.BLACK, Color.WHITE, Color.BLACK))
    }
}
