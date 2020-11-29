package ch.scorpion.jabbah.app

/**
 * An object that can be saved by [Application.save].
 */
interface Savable {

    /**
     * Holds the description of this [Savable] that can be displayed in the UI, such as a file name to be
     * displayed in a main frame's title bar.
     */
    val description: String

    /**
     * Determines whether this [Savable] is completely defined, i.e. it can be saved without further interaction
     * with the user, such as asking for a file name.
     * @return `true` if this [Savable]] is completely defined.
     */
    val defined: Boolean

	val notDefined: Boolean get() = !defined

    /** Determines whether this [Savable] can be added to the "Most Recent" file menu.*/
    val supportsMostRecent: Boolean

	/**
	 * Determines whether this [Savable] is editable by the current user, which typically depends on
	 * the ownership of this [Savable].
	 * */
	val editable: Boolean

    /**
     * Opens this [Savable] in the specified [Application].
     * @param application the [Application] in which this [Savable] is opened.
     * @return `true` if this [Savable] has been opened, `false` if the open process has been aborted by the user
     */
    fun open(application: Application): Boolean

    /**
     * Saves this [Savable] using the specified [Application].
     * @param application the [Application] within which this [Savable] is saved.
     * @return `true` if this [Savable] has been saved, `false` if the save process has been aborted by the user
     */
    @Deprecated("Replaced by method using controller")
    fun save(application: Application): Boolean

	/**
	 * Saves this [Savable] using the specified [ApplicationDataViewController].
	 * @param appDataViewController the [ApplicationDataViewController] within which this [Savable] is saved.
	 * @return `true` if this [Savable] has been saved, `false` if the save process has been aborted by the user
	 */
	fun save(appDataViewController: ApplicationDataViewController): Boolean
}

/** Posted by [Application] when its current [Savable] has changed.*/
data class CurrentSavableEvent(val savable: Savable?)