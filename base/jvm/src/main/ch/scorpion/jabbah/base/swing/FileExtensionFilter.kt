package ch.scorpion.jabbah.base.swing

import org.apache.commons.io.FilenameUtils
import java.io.File
import javax.swing.filechooser.FileFilter
import javax.swing.JFileChooser

/**
 * Filters file name to file with a given extension to be used in [JFileChooser].
 */
class FileExtensionFilter(private val extension: String, private val displayName: String) : FileFilter() {

	override fun accept(file: File): Boolean {
		if (file.isDirectory) {
			return true
		}
		val extension = FilenameUtils.getExtension(file.name)
		if (extension != null) {
			return extension == this.extension
		}
		return false
	}

	override fun getDescription(): String {
		return displayName
	}
}