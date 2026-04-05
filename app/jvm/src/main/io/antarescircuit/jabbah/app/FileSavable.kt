package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.Translations
import java.io.File

/**
 * A [Savable] that saves application data into a [File] of a file system.
 * Can only be used with [DesktopApplication].
 */
data class FileSavable(val filePath: String?) : DefaultSavable(filePath) {

	override val typeName: String get() = Translations.getString("application.savable.name")

    companion object {

        fun undefined(): FileSavable {
            return FileSavable(null)
        }

        fun withPath(filePath: String): FileSavable {
            return FileSavable(filePath)
        }
    }
}