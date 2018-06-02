package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.io.Storable

/**
 * The main class of a running application program, and the main point for controlling
 * the application data that is managed by this application.
 */
interface Application {

    /** Holds the displayable name of this [Application].*/
    val displayName: String

    /**
     * Holds the system name to be used in home directories, property files and log file.
     * Returns the [displayName] as default.
     */
    val systemName: String get() = displayName

    /** Holds the current [Savable].*/
    var savable: Savable?

    /** Holds the currently open application data.*/
    var applicationData: Storable?

    /** Determines whether the current application data has been changed.*/
    val applicationDataChanged: Boolean

    val mostRecentSavables: SavableHistory

    /**
     * Starts this [Application] by initializing it, by loading predefined content, and by displaying its primary view.
     * This method is typically implemented in a platform-specific layer.
     */
    fun start()

	/** Creates a new [applicationData] object and resets the current [Savable].*/
    fun newFile()

	/** Saves the current [applicationData] at the location indicated by [savable].*/
    fun save()

	/** Asks the user to define a [Savable] and uses it to save the current [applicationData].*/
	fun saveAs(): Boolean

	/** Registers the specified [Storable] as current [applicationData] and the specified [Savable] as current [savable].*/
	fun open(storable: Storable, savable: Savable)

	/** Asks the user to choose a [Savable] and opens it.*/
	fun open()
}