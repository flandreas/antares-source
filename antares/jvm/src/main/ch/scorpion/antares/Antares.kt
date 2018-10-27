package ch.scorpion.antares

import ch.scorpion.jabbah.app.DesktopApplication
import java.nio.file.FileSystems
import java.nio.file.Path

interface Antares : DesktopApplication {

	companion object {
		private const val DISPLAY_NAME = "Antares"
		private const val SYSTEM_NAME = "antares"
		private const val FILE_EXTENSION_NAME = "cir"
		private const val DEFAULT_LIB_DIRECTORY = "libraries"
		private const val DEFAULT_PROJECT_DIRECTORY = "projects"
		private const val DEFAULT_LIB_FILENAME = "library.lib"
	}

	/** ---- [DesktopApplication] */

	override val displayName: String get() = DISPLAY_NAME

	override val systemName: String get() = SYSTEM_NAME

	override val fileExtension: String get() = FILE_EXTENSION_NAME

	/** ---- [Antares] */

	val projectsDirectoryPath: Path get() = FileSystems.getDefault().getPath(homeDirectoryPath.toString(), DEFAULT_PROJECT_DIRECTORY)

	val libraryDirectoryPath: Path get() = FileSystems.getDefault().getPath(homeDirectoryPath.toString(), DEFAULT_LIB_DIRECTORY)

	val libraryFileName: String get() = DEFAULT_LIB_FILENAME

}
