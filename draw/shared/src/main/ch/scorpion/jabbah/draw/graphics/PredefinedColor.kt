package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.logger

enum class PredefinedColorIdentity(
	val idName: String,
	private val descriptionKey: String
) {

	White("white", "graphics.color.white.name"),
	Black("black", "graphics.color.black.name"),
	Gray("gray", "graphics.color.gray.name"),
	Red("red", "graphics.color.red.name"),
	Blue("blue", "graphics.color.blue.name"),
	Green("green", "graphics.color.green.name"),
	Yellow("yellow", "graphics.color.yellow.name");

	companion object {
		fun containsIdName(idName: String): Boolean = values().map { it.idName }.contains(idName)
		fun withIdName(idName: String): PredefinedColorIdentity = values().first { it.idName == idName }
	}

	/** Returns the description of the color in the user's language.*/
	val description: String get() = Translations.getString(descriptionKey)

}
/**
 * A [PredefinedColor] is a [CompositeColor] with a particular name that has been predefined by
 * the developer of an application and that is part of a set of harmonic colors from which the user
 * can choose to color his graphical objects.
 */
data class PredefinedColor(val identity: PredefinedColorIdentity, val color: CompositeColor) {

	val name: String get() = identity.idName

    /** Returns the description of the color in the user's language.*/
    val description: String get() = identity.description

    override fun toString(): String = description
}

interface PredefinedColorProvider {

    /** Returns the [List] of all known [PredefinedColor]s.*/
    fun provideAll(): List<PredefinedColor>

    /** Returns the [PredefinedColor] with the specified [PredefinedColorIdentity] name, or `null` if not available.*/
    fun withIdName(idName: String): PredefinedColor?

	/** Returns the [PredefinedColor] with the specified identity, or `null` if not available.*/
	fun withIdentity(identity: PredefinedColorIdentity): PredefinedColor?
}

/**
 * An implementation of a [PredefinedColorProvider] that allows to register [PredefinedColor]s programmatically.
 * Currently implemented as a singleton object because no multi-target DI container is available.
 */
object PredefinedColorRepository : PredefinedColorProvider {

    val LOG by logger(PredefinedColorRepository::class)

    /** Contains the registered [PredefinedColor]s.*/
    private val colorsList: MutableList<PredefinedColor> by lazy { mutableListOf()}

	private val colors: MutableMap<PredefinedColorIdentity,PredefinedColor> by lazy { mutableMapOf() }

    /** ---- [PredefinedColorProvider] interface */

    override fun provideAll(): List<PredefinedColor> {
        return colorsList.toImmutableList()
    }

    override fun withIdName(idName: String): PredefinedColor? {
	    return if (PredefinedColorIdentity.containsIdName(idName)) {
		    colors[PredefinedColorIdentity.withIdName(idName)]
	    } else {
		    null
	    }
    }

	override fun withIdentity(identity: PredefinedColorIdentity): PredefinedColor? = colors[identity]

    /** ---- [PredefinedColorRepository] */

    /** Clears all registrations.*/
    fun clear() {
        colors.clear()
	    colorsList.clear()
    }

    fun register(color: PredefinedColor) {
        if (containsName(color.name)) {
            colorsList[colorsList.indexOf(withIdName(color.name))] = color
        } else {
            colorsList.add(color)
        }
        colors[color.identity] = color
    }

    private fun containsName(name: String): Boolean {
        return withIdName(name) != null
    }
}