package io.antarescircuit.jabbah.base.swing

import org.apache.commons.io.FilenameUtils
import java.io.File
import javax.swing.filechooser.FileFilter
import javax.swing.JFileChooser

/**
 * Filters file name to file with a given extension to be used in [JFileChooser].
 */
class FileExtensionFilter(
	private val extensions: Set<String>,
	private val displayName: String
) : FileFilter() {

	constructor(extension: String, displayName: String) : this(setOf(extension), displayName)

	override fun accept(file: File): Boolean {
		if (file.isDirectory) {
			return true
		}
		val extension = FilenameUtils.getExtension(file.name)
		if (extension != null) {
			return extensions.any { it.lowercase() == extension.lowercase() }
		}
		return false
	}

	override fun getDescription(): String = displayName
}