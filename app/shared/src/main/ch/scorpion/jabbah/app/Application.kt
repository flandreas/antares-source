package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Translations
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

	var data: ApplicationData?

    /** Determines whether the current application data has been changed.*/
    val applicationDataChanged: Boolean

    val mostRecentSavables: SavableHistory

	val aboutInfo: AboutInfo

    /**
     * Starts this [Application] by initializing it, by loading predefined content, and by displaying its primary view.
     * This method is typically implemented in a platform-specific layer.
     */
    fun start()

	/** Creates a new [ApplicationData] object and resets the current [Savable].*/
    fun newFile()

	/** Saves the current [ApplicationData] at the location indicated by its [Savable].*/
    fun save()

	/** Asks the user to define a [Savable] and uses it to save the current [ApplicationData] content.*/
	fun saveAs(): Boolean

	/** Registers the specified [ApplicationData] as the current one.*/
	fun open(data: ApplicationData)

	/** Asks the user to choose a [Savable] and opens it.*/
	fun open()

	/** Closes the current [ApplicationData]. */
	fun close()

	fun showAboutInfo()

}

data class AboutInfo(
	val iconPath: String?,
	val name: String,
	val claim: String,
	val version: String,
	val disclaimer: String = Translations.getString("application.disclaimer.allRightsReserved")
)