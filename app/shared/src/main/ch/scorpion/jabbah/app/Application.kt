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

    /** Starts this [Application] by initializing it and by loading predefined content.*/
    fun start()

    fun newFile()

    fun save()

    fun open(storable: Storable, savable: Savable)
}