package ch.scorpion.antares

import ch.scorpion.antares.AntaresApplication.Companion.DEFAULT_LIB_DIRECTORY
import ch.scorpion.antares.AntaresApplication.Companion.DEFAULT_LIB_FILENAME
import ch.scorpion.antares.AntaresApplication.Companion.DEFAULT_PROJECT_DIRECTORY
import ch.scorpion.antares.AntaresApplication.Companion.DOC_URL
import ch.scorpion.antares.AntaresApplication.Companion.FILE_EXTENSION_NAME
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.graph.library.Library
import java.nio.file.FileSystems

interface AntaresDesktop : AntaresApplication, DesktopApplication {

	override val documentationUrl: String? get() = DOC_URL

	/** ---- [DesktopApplication] */


	override val fileExtension: String get() = FILE_EXTENSION_NAME

	/** ---- [AntaresDesktop] */

	val projectsDirectoryPath: String get() = FileSystems.getDefault().getPath(userDataDirectoryPath.toString(), DEFAULT_PROJECT_DIRECTORY).toString()

	val userLibraryDirectoryPath: String get() = FileSystems.getDefault().getPath(userDataDirectoryPath.toString(), DEFAULT_LIB_DIRECTORY).toString()

	/**
	 * Returns the optional path to the file system directory where the system [Libraries][Library] are stored.
	 * If `null`, the system [Libraries][Library] are read from the program's resource folder, which is the default
	 * for user installations. Because the resource folder is read-only, a developer who wants to edit the system
	 * [Libraries][Library] with the UI, he can set this property to point to the library directory under
	 * source control (git). The module system will the use the corresponding persistence services that allow to
	 * edit all relevant system library files.
	 */
	val systemLibraryDirectoryPath: String?

	val libraryFileName: String get() = DEFAULT_LIB_FILENAME

}
