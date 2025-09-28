package ch.scorpion.jabbah.app

import java.nio.file.Path

/**
 * A [DesktopApplication] is an application that is installed on a desktop computer and uses a
 * file system (typically the computer's local file system) to store [Savable]s.
 *
 * Different implementations of [DesktopApplication] can use different strategies for managing stored [Savable]s.
 * Some might store [Savable]s in individual files at a fully qualified file path, while others might implement
 * a kind of repository whose entries are identified by a simple name.
 */
interface DesktopApplication : Application {

	/** Returns the file name extension to be used for application data files handled by this [Application].*/
	val fileExtension: String

	/** The [Path] to the directory where this [DesktopApplication] stores config and log files.*/
	val appDataDirectoryPath: Path

	/**
	 * The [Path] to the directory where this [DesktopApplication] stores config and log files by default.
	 * This is chosen when the application is installed, and its location depends on the OS.
	 * Event if the concrete [DesktopApplication] offers a feature where the user can overwrite this location,
	 * this property always returns the system default. Can be used to reset the user setting to the default.
	 */
	val defaultUserDataDirectoryPath: Path

	/** The name of the [Application]'s log file. */
	val logFileName: String

	val version: ApplicationVersion

	fun exportLogfile(destinationPath: String)

	/**
	 * Request to quit the [Application], which can be denied by the user.
	 * @return `true` if quitting was confirmed by the user, or confirmation was not required.
	 */
	fun quit(): Boolean
}