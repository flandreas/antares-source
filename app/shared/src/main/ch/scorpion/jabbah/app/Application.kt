package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Translations

/**
 * The main class of a running application program, and the main point for controlling
 * the application data that is managed by this application.
 */
interface Application {

	val controller: ApplicationDataViewController

    /** Holds the displayable name of this [Application].*/
    val displayName: String

    /**
     * Holds the system name to be used in home directories, property files and log file.
     * Returns the [displayName] as default.
     */
    val systemName: String get() = displayName

	val aboutInfo: AboutInfo

    /**
     * Starts this [Application] by initializing it, by loading predefined content, and by displaying its primary view.
     * This method is typically implemented in a platform-specific layer.
     */
    fun start()

	fun showAboutInfo()
}

data class AboutInfo(
	val iconPath: String?,
	val name: String,
	val claim: String,
	val version: String,
	val disclaimer: String = Translations.getString("application.disclaimer.allRightsReserved")
)