package ch.scorpion.jabbah.app

import java.io.File
import javax.swing.filechooser.FileFilter

/**
 * Filters file name to file with the file extension of an [Application].
 */
class ApplicationFileFilter(private val extension: String, private val displayName: String) : FileFilter() {

    constructor(application: DesktopApplication): this(application.fileExtension, application.displayName)

    override fun accept(file: File): Boolean {
        if (file.isDirectory) {
            return true
        }
        val extension = getExtension(file)
        if (extension != null) {
            return extension == this.extension
        }
        return false
    }

    override fun getDescription(): String {
        return displayName
    }

    private fun getExtension(file: File): String? {
        var ext: String? = null
        val s = file.name
        val i = s.lastIndexOf('.')

        if (i > 0 && i < s.length - 1) {
            ext = s.substring(i + 1).toLowerCase()
        }
        return ext
    }
}