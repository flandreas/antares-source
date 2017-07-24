package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.logger

/**
 * A [PredefinedColor] is a [CompositeColor] with a particular name that has been predefined by
 * the developer of an application and that is part of a set of harmonic colors from which the user
 * can choose to color his graphical objects.
 */
data class PredefinedColor(val name: String, val descriptionKey: String, val color: CompositeColor) {

    /** Returns the description of the color in the user's language.*/
    val description: String get() = Translations.getString(descriptionKey)

    override fun toString(): String = description
}

interface PredefinedColorProvider {

    /** Returns the [List] of all known [PredefinedColor]s.*/
    fun provideAll(): List<PredefinedColor>

    /** Returns the [PredefinedColor] with the specified name, or `null` if not available.*/
    fun withName(name: String): PredefinedColor?
}

/**
 * An implementation of a [PredefinedColorProvider] that allows to register [PredefinedColor]s programmatically.
 * Currently implemented as a singleton object because no multi-target DI container is available.
 */
object PredefinedColorRepository : PredefinedColorProvider {

    val LOG by logger(PredefinedColorRepository::class)

    /** Contains the registered [PredefinedColor]s.*/
    val colors: MutableList<PredefinedColor> by lazy { mutableListOf<PredefinedColor>()}

    /** ---- [PredefinedColorProvider] interface */

    override fun provideAll(): List<PredefinedColor> {
        return colors.toImmutableList()
    }

    override fun withName(name: String): PredefinedColor?= colors.find { it.name == name }

    /** ---- [PredefinedColorRepository] */

    /** Clears all registrations.*/
    fun clear() {
        colors.clear()
    }

    fun register(color: PredefinedColor) {
        if (containsName(color.name)) {
            LOG.warn("PredefinedColor with name ${color.name} already registered")
            colors[colors.indexOf(withName(color.name))] = color
        } else {
            colors.add(color)
        }
    }

    private fun containsName(name: String): Boolean {
        return withName(name) != null
    }
}