package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.Translations

/**
 * Defines names for a predefined set of [Style]s.
 *
 * Higher framework layers might subclass this class in order to define more [StyleType]s.
 *
 * @property name the name of this [StyleType]. Used as ID when made persistent.
 * @property descriptionKey the translation key of this [StyleType]. Used when offered in the UI to be chosen by user.
 * @property isSystem `true` if only used by system and should not be offered in the UI to be chosen by user.
 */
open class StyleType(
	val name: String,
	val descriptionKey: String,
	val isSystem: Boolean = false,
	val isBackdrop: Boolean = false
) {

    companion object {
        val FIGURE = StyleType("figure", "draw.styleType.figure.name")
	    val ANNOTATION = StyleType("annotation", "draw.styleType.annotation.name")
        val BACKGROUND = StyleType("background", "draw.styleType.background.name", true)
        val TOOLTIP = StyleType("tooltip", "draw.styleType.tooltipView.name", true)
    }

    val description: String get() = Translations.getString(descriptionKey)

    override fun toString(): String = description
}