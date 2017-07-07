package ch.scorpion.jabbah.app

/**
 * An object that can be saved by [Application.save].
 */
interface Savable {

    /**
     * Holds the description of this [Savable* that can be displayed in the UI, such as a file name to be
     * displayed in a main frame's title bar.
     */
    val description: String

    /**
     * Determines whether this [Savable] is completly defined, i.e. it can be saved without further interaction
     * with the user, such as asking for a file name.
     * @return `true` if this [Savable]] is completly defined.
     */
    val defined: Boolean

    /**
     * Saves this [Savable] using the specified [Application].
     * @param application the [Application] within which this [Savable] is saved.
     * @return `true` if this [Savable] has been saved, `false` if the save process has been aborted by the user
     */
    fun save(application: Application): Boolean
}

/** Posted by [Application] when its current [Savable] has changed.*/
data class CurrentSavableEvent(val application: Application, val savable: Savable?)