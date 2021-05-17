package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.ZipUtil
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.MetaGraph
import org.apache.commons.io.FileUtils
import java.io.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Persists a [Library] in a user's local file system directory.
 *
 * @property directoryPath the absolute path of the file system directory where the [Libraries][Library] are located
 * @property metaGraphFileExtension the file name extension of [MetaGraph] files
 * @property libraryFileName the name of the [Library] file
 */
class FileLibraryPersistenceService(
	private val directoryPath: String,
	private val metaGraphFileExtension: String = DEF_META_GRAPH_FILE_EXTENSION,
	private val libraryFileName: String = DEF_LIBRARY_FILE_NAME
) : AbstractFileLibraryPersistenceService() {

	companion object {
		private val LOG by logger(FileLibraryPersistenceService::class)
	}

	/** ---- [LibraryPersistenceService] */

	override fun deleteMetaGraph(library: Library, uuid: UUID) {
		LOG.trace("delete MetaGraph $uuid in Library ${library.uuid}")
		File(buildMetaGraphFilePath(library.uuid, uuid)).delete()
	}

	override fun loadLibrary(uuid: UUID): Library {
		LOG.trace("load Library $uuid")
		return createLibraryFileInputStream(uuid).use {
			loadLibrary(uuid, it)
		}
	}

	override fun deleteLibrary(uuid: UUID) {
		LOG.trace("delete Library $uuid")
		FileUtils.deleteDirectory(File(buildLibraryDirectoryPath(uuid)))
	}

	private fun copyUserLibrary(uuid: UUID, destinationDirectory: String) {
		val sourceDirectory = buildLibraryDirectoryPath(uuid)
		copyLibrary(sourceDirectory, destinationDirectory)
	}

	private fun copyLibrary(sourceDirectory: String, destinationDirectory: String) {
		FileUtils.copyDirectory(
			File(sourceDirectory),
			File(destinationDirectory)
		)
	}

	override fun exportLibrary(uuid: UUID, outputPath: String) {
		LOG.trace("Exporting library to $outputPath")
		FileOutputStream(outputPath).use { output ->
			ZipOutputStream(output).use {
				val fileToZip = File(buildLibraryDirectoryPath(uuid))
				ZipUtil.zipFile(fileToZip, fileToZip.name, it)
			}
		}
	}

	override fun exportLibraryTemporarily(uuid: UUID): String {
		val tempDir = Files.createTempDirectory(null)
		val outputLibDir = Path.of(tempDir.toString(), uuid.toString()).toAbsolutePath().toString()
		copyUserLibrary(uuid, outputLibDir)

		return outputLibDir
	}

	override fun importTemporaryLibrary(uuid: UUID, temporaryPath: String) {
		copyLibrary(temporaryPath, buildLibraryDirectoryPath(uuid))
	}

	override fun importLibrary(inputPath: String): UUID {
		LOG.trace("Importing library from $inputPath")

		// Import and unzip file to incubation directory
		val incubationDirPath = Files.createTempDirectory(null)
		FileInputStream(inputPath).use { input ->
			ZipInputStream(input).use {
				ZipUtil.unzipFile(incubationDirPath, it)
			}
		}

		// Check created directory structure
		val incubationDir = incubationDirPath.toFile()
		val incubationFiles = incubationDir.listFiles()
		if (incubationFiles == null || incubationFiles.size != 1) {
			val msg = "Expected 1 file in zip file, but found ${incubationFiles?.size}"
			LOG.trace(msg)
			throw IllegalArgumentException(msg)
		}

		// Load incubating Library
		val libraryFilePath = buildLibraryFilePath(incubationDirPath.toAbsolutePath().toString(), incubationFiles[0].name)
		val library = createLibraryFileInputStream(libraryFilePath).use {
			try {
				loadLibrary(it)
			} catch (e: Exception) {
				LOG.debug("Could not read library file, possibly not an Graph library")
				throw IllegalArgumentException("Could not read library file", e)
			}
		}

		// Check if UUID already exists
		val newDirectory = Paths.get(buildLibraryDirectoryPath(library.uuid))
		if (Files.exists(newDirectory)) {
			val msg = "Library ${library.uuid} already exists"
			LOG.debug(msg)
			throw IllegalStateException(msg)
		}

		// Rename directory to UUID of imported Library
		incubationFiles[0].renameTo(newDirectory.toFile())

		return library.uuid
	}

	/** ---- [AbstractFileLibraryPersistenceService] */

	override fun ensureLibraryDirectory(libraryUuid: UUID) {
		val path = Paths.get(buildLibraryDirectoryPath(libraryUuid))
		if (!Files.exists(path)) {
			Files.createDirectories(path)
		}
	}

	override fun createMetaGraphInputStream(libraryUuid: UUID, metaGraphUuid: UUID): InputStream {
		val path = buildMetaGraphFilePath(libraryUuid, metaGraphUuid)
		try {
			return FileInputStream(path)
		} catch (e: FileNotFoundException) {
			LOG.error("MetaGraph file $path not found")
			throw e
		}
	}

	override fun createMetaGraphOutputStream(libraryUuid: UUID, metaGraphUuid: UUID): OutputStream {
		val path = buildMetaGraphFilePath(libraryUuid, metaGraphUuid)
		try {
			return FileOutputStream(path)
		} catch (e: FileNotFoundException) {
			LOG.error("MetaGraph file $path not found")
			throw e
		}
	}

	override fun createLibraryFileInputStream(libraryUuid: UUID): InputStream =
		createLibraryFileInputStream(buildLibraryFilePath(libraryUuid))

	private fun createLibraryFileInputStream(path: String): InputStream {
		try {
			return FileInputStream(path)
		} catch (e: FileNotFoundException) {
			LOG.error("Library file $path not found")
			throw e
		}
	}

	override fun createLibraryFileOutputStream(libraryUuid: UUID): OutputStream {
		val path = buildLibraryFilePath(libraryUuid)
		try {
			return FileOutputStream(path)
		} catch (e: FileNotFoundException) {
			LOG.error("Library file $path not found")
			throw e
		}
	}

	/** ---- [FileLibraryPersistenceService] */

	private fun buildMetaGraphFilePath(libraryUUID: UUID, metaGraphUuid: UUID): String =
		FileSystems.getDefault().getPath(directoryPath, libraryUUID.toString(), "$metaGraphUuid.$metaGraphFileExtension").toString()

	private fun buildLibraryFilePath(libraryUuid: UUID): String = buildLibraryFilePath(directoryPath, libraryUuid.toString())

	private fun buildLibraryFilePath(directoryPath: String, libraryDirName: String): String =
		FileSystems.getDefault().getPath(directoryPath, libraryDirName, libraryFileName).toString()

	private fun buildLibraryDirectoryPath(libraryUuid: UUID): String =
		FileSystems.getDefault().getPath(directoryPath, libraryUuid.toString()).toString()
}
