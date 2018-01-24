package ch.scorpion.jabbah.app

/**
 * Created by andreas on 13.02.17.
 */
interface DesktopApplication : Application {

    /** Holds the main [AbstractApplicationFrame] of this [DesktopApplication].*/
    val mainFrame: AbstractApplicationFrame

    /** Returns the file name extension to be used for application data files handled by this [Application].*/
    val fileExtension: String

    fun saveAs(): Boolean

    fun saveFile(filePath: String)

    fun openFile(filePath: String): Boolean

    fun quit()
}