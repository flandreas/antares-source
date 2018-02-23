package ch.scorpion.jabbah.app

import java.nio.file.Path

/**
 * Created by andreas on 13.02.17.
 */
interface DesktopApplication : Application {

	/** Returns the file name extension to be used for application data files handled by this [Application].*/
	val fileExtension: String

	val homeDirectoryPath: Path

	fun saveFile(filePath: String)

	fun openFile(filePath: String): Boolean

	fun quit()
}