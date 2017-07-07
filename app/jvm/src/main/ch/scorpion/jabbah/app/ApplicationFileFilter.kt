package ch.scorpion.jabbah.app

import java.io.File
import javax.swing.filechooser.FileFilter

/**
 * Filters file name to file with the file extension of an [Application]
 */
class ApplicationFileFilter(val application: DesktopApplication) : FileFilter() {

    override fun accept(file: File): Boolean {
        if (file.isDirectory()) {
            return true
        }
        val extension = getExtension(file)
        if (extension != null) {
            return extension == application.fileExtension
        }
        return false
    }

    override fun getDescription(): String {
        return application.displayName
    }

    private fun getExtension(file: File): String? {
        var ext: String? = null
        val s = file.getName()
        val i = s.lastIndexOf('.')

        if (i > 0 && i < s.length - 1) {
            ext = s.substring(i + 1).toLowerCase()
        }
        return ext
    }
}