package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalStateException
import java.util.*

/**
 * A [Savable] that saves application data into a [File] of a file system.
 */
data class FileSavable(val filePath: String?) : Savable {

    companion object {

        fun undefined(): FileSavable {
            return FileSavable(null)
        }

        fun withPath(filePath: String): FileSavable {
            return FileSavable(filePath)
        }
    }

    /** ---- [FileSavable] interface */

    override val description: String
        get() {
            val sb = StringBuilder(Translations.getString("application.fileSaveable.prefix"))
            if (filePath == null) {
                sb.append(" <")
                sb.append(Translations.getString("application.title.unknown"))
                sb.append(">")
            } else {
                sb.append(" ")
                sb.append(filePath)
            }
            return sb.toString()
        }

    override val defined: Boolean get() = filePath != null && !filePath.isEmpty()

    override fun open(application: Application): Boolean {
        val desktopApplication = application as DesktopApplication
        if (!defined) {
            throw IllegalStateException("cannot open undefined FileSavable")
        }
        return desktopApplication.openFile(filePath!!)
    }

    override fun save(application: Application): Boolean {
        val desktopApplication = application as DesktopApplication
        if (defined) {
            desktopApplication.saveFile(filePath!!)
            return true
        }
        return desktopApplication.saveAs()
    }
}