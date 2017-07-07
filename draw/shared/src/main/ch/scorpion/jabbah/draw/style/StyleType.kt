package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.Translations

/**
 * Defines names for a predefined set of [Style]s.
 *
 * Higher framework layers might subclass this class in order to define more [StyleType]s.
 */
open class StyleType(val name: String, val descriptionKey: String) {

    companion object {
        val FIGURE: StyleType = StyleType("figure", "draw.styleType.figure.name")
        val BACKGROUND: StyleType = StyleType("background", "draw.styleType.background.name")
    }

    val description: String get() = Translations.getString(descriptionKey)

    override fun toString(): String = description
}