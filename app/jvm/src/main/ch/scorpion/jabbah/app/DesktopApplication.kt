package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import java.nio.file.Path

/**
 * A [DesktopApplication] is an application that is installed on a desktop computer and uses a
 * file system (typically the computer's local file system) to store [Savable]s.
 *
 * Different implementations of [DesktopApplication] can use different strategies for managing stored [Savable]s.
 * Some might store [Savable]s in individual files at a fully qualified file path, while others might implement
 * a kind of a repository whose entries are identified by a simple name.
 */
interface DesktopApplication : Application {

	/** Returns the file name extension to be used for application data files handled by this [Application].*/
	val fileExtension: String

	/** The [Path] to the user's data directory, i.e. the directory where the user's data is stored.*/
	val userDataDirectoryPath: Path

	fun saveTo(identification: String)

	/**
	 * Opens a [Savable] from the persistent location identified by [identification]. Depending on the
	 * type of storage management used by the concrete [DesktopApplication] implementation,
	 * this could be a file system path, a simple name, or anything else.
	 * @return `true` if the [Savable] has been opened from `identification`, `false` if not, e.g. because
	 * the user denied to replace unsaved data with the [Savable] to open
	 * @throws IllegalArgumentException if `identification` doesn't exist
	 */
	fun openFrom(identification: String): Boolean

	fun exportLogfile(destinationPath: String)

	fun quit()
}