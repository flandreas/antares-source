package ch.scorpion.jabbah.app

import java.io.File

/**
 * A [Savable] that saves application data into a [File] of a file system.
 * Can only be used with [DesktopApplication].
 */
data class FileSavable(val filePath: String?) : DefaultSavable(filePath) {

    companion object {

        fun undefined(): FileSavable {
            return FileSavable(null)
        }

        fun withPath(filePath: String): FileSavable {
            return FileSavable(filePath)
        }
    }
}