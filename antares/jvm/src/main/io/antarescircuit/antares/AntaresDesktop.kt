package io.antarescircuit.antares

import io.antarescircuit.antares.AntaresApplication.Companion.DEFAULT_LIB_DIRECTORY
import io.antarescircuit.antares.AntaresApplication.Companion.DEFAULT_LIB_FILENAME
import io.antarescircuit.antares.AntaresApplication.Companion.DEFAULT_NON_VOLATILE_DIRECTORY
import io.antarescircuit.antares.AntaresApplication.Companion.DEFAULT_PROJECT_DIRECTORY
import io.antarescircuit.antares.AntaresApplication.Companion.DOC_URL_DEV
import io.antarescircuit.antares.AntaresApplication.Companion.DOC_URL_PROD
import io.antarescircuit.antares.AntaresApplication.Companion.FILE_EXTENSION_NAME
import io.antarescircuit.antares.AntaresApplication.Companion.ISSUES_URL_DEV
import io.antarescircuit.antares.AntaresApplication.Companion.ISSUES_URL_PROD
import io.antarescircuit.antares.AntaresApplication.Companion.YOUTUBE_CHANNEL_URL
import io.antarescircuit.jabbah.app.DesktopApplication
import io.antarescircuit.jabbah.app.Environment
import io.antarescircuit.jabbah.base.DataLocation
import io.antarescircuit.jabbah.graph.library.Library

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

	override val youtubeChannelUrl: String? get() = YOUTUBE_CHANNEL_URL

	/** ---- [io.antarescircuit.jabbah.app.DesktopApplication] */


	override val fileExtension: String get() = FILE_EXTENSION_NAME

	/** ---- [AntaresDesktop] */

	val projectDirectoryName: String get() = DEFAULT_PROJECT_DIRECTORY

	val userLibraryDirectoryName: String get() = DEFAULT_LIB_DIRECTORY

	val nonVolatileDirectoryName: String get() = DEFAULT_NON_VOLATILE_DIRECTORY

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
