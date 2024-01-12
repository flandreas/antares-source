package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.logger
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Imports a [Library] from a ZIP file into a file store.
 */
class FileLibraryImporter(
	private val service: FileLibraryPersistenceService
) : AbstractLibraryImporter() {

	companion object {
		private val LOG by logger(FileLibraryImporter::class)
	}

	override suspend fun checkIfUuidAlreadyExists(identification: LibraryIdentification, replaceExisting: Boolean): Boolean {
		val newDirectory = Paths.get(service.buildLibraryDirectoryPath(identification))
		val exists = Files.exists(newDirectory)
		if (exists && !replaceExisting) {
			val msg = "Library ${identification.uuid} already exists"
			LOG.trace(msg)
			throw LibraryImportConflictException(identification)
		}
		return exists
	}

	override suspend fun save(library: Library, sourceLibraryDir: File, exists: Boolean) {
		val newDirectory = Paths.get(service.buildLibraryDirectoryPath(library.identification))
		if (exists) {
			FileUtils.deleteDirectory(newDirectory.toFile())
		}
		Files.move(sourceLibraryDir.toPath(), newDirectory, StandardCopyOption.REPLACE_EXISTING)
	}
}