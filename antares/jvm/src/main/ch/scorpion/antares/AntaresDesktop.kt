package ch.scorpion.antares

import ch.scorpion.antares.AntaresApplication.Companion.DEFAULT_LIB_DIRECTORY
import ch.scorpion.antares.AntaresApplication.Companion.DEFAULT_LIB_FILENAME
import ch.scorpion.antares.AntaresApplication.Companion.DEFAULT_PROJECT_DIRECTORY
import ch.scorpion.antares.AntaresApplication.Companion.DOC_URL_DEV
import ch.scorpion.antares.AntaresApplication.Companion.DOC_URL_PROD
import ch.scorpion.antares.AntaresApplication.Companion.FILE_EXTENSION_NAME
import ch.scorpion.antares.AntaresApplication.Companion.ISSUES_URL_DEV
import ch.scorpion.antares.AntaresApplication.Companion.ISSUES_URL_PROD
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.Environment
import ch.scorpion.jabbah.base.DataLocation
import ch.scorpion.jabbah.graph.library.Library

interface AntaresDesktop : AntaresApplication, DesktopApplication {

	override val documentationUrl: String? get() =
		when (environment) {
			Environment.Development -> DOC_URL_DEV
			Environment.Production -> DOC_URL_PROD
		}

	override val issuesUrl: String? get() =
		when (environment) {
			Environment.Development -> ISSUES_URL_DEV
			Environment.Production -> ISSUES_URL_PROD
		}

	/** ---- [DesktopApplication] */


	override val fileExtension: String get() = FILE_EXTENSION_NAME

	/** ---- [AntaresDesktop] */

	val fileStoreBasePath: String get() = userDataDirectoryPath.toString()

	val projectDirectoryName: String get() = DEFAULT_PROJECT_DIRECTORY

	val userLibraryDirectoryName: String get() = DEFAULT_LIB_DIRECTORY

	/**
	 * Returns the optional path to the file system base directory containing the [Libraries][Library] directory.
	 * Will be expanded by the default [Library] directory name.
	 *
	 * If `null`, the system [Libraries][Library] are read from the program's resource folder, which is the default
	 * for user installations. Because the resource folder is read-only, a developer who wants to edit the system
	 * [Libraries][Library] with the UI, he can set this property to point to the library directory under
	 * source control (git). The module system will the use the corresponding persistence services that allow to
	 * edit all relevant system library files.
	 */
	val systemLibraryBasePath: String?

	val libraryFileName: String get() = DEFAULT_LIB_FILENAME

	val dataLocation: DataLocation
}
