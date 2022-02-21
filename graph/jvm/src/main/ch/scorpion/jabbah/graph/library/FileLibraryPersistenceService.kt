package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.ZipUtil
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.edit.auth.UserHolder
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.GraphQuotaException
import ch.scorpion.jabbah.graph.MetaGraph
import org.apache.commons.io.FileUtils
import java.io.*
import java.nio.file.*
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Persists a [Library] in a user's local file system directory.
 *
 * @property dataPath the absolute path of the file system directory where the data is stored
 * @property directoryName the name of the directory within [dataPath] that holds the [Library] files (e.g. "libraries")
 * @property metaGraphFileExtension the file name extension of [MetaGraph] files
 * @property libraryFileName the name of the [Library] file
 * @property userHolder if provided, the identification of the [User] is part of the file system path
 */
@Suppress("MemberVisibilityCanBePrivate")
class FileLibraryPersistenceService(
	private val dataPath: String,
	private val directoryName: String,
	private val metaGraphFileExtension: String = DEF_META_GRAPH_FILE_EXTENSION,
	private val libraryFileName: String = DEF_LIBRARY_FILE_NAME,
	private val userHolder: UserHolder<User>? = null
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
		exportLibrary(uuid, FileOutputStream(outputPath))
	}

	fun exportLibrary(uuid: UUID, outputStream: FileOutputStream) {
		outputStream.use { output ->
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

	override fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library {
		LOG.trace("Importing library from $inputPath")
		return importLibrary(FileInputStream(inputPath), replaceExisting = false, currentLibraryCount, quota)
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

	private val baseName: String get() {
		val sep = FileSystems.getDefault().separator
		return if (userHolder == null) {
			"$dataPath$sep$directoryName"
		} else {
			"$dataPath$sep${userHolder.user.identity}$sep$directoryName"
		}
	}

	/**
	 * Importing a [Library] by reading its data from [inputStream], replacing it if
	 * it already exists if [replaceExisting] is `true`.
	 *
	 * @throws IllegalArgumentException if an error occurred while reading from [inputStream]
	 * @throws LibraryImportConflictException if a [Library] with the [UUID] of the imported [Library] already exists
	 * and [replaceExisting] is false
	 * @throws GraphQuotaException if the user's [GraphQuota] are not sufficient to import the [Library]
	 * @return the imported [Library]
	 */
	fun importLibrary(
		inputStream: InputStream,
		replaceExisting: Boolean,
		currentLibraryCount: Int,
		quota: GraphQuota = GraphQuota.UNLIMITED
	): Library {

		// Import and unzip file to incubation directory
		val incubationDirPath = Files.createTempDirectory(null)
		inputStream.use { input ->
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
				LOG.trace("Could not read library file, possibly not an Graph library")
				throw IllegalArgumentException("Could not read library file", e)
			}
		}

		// Check if UUID already exists
		val newDirectory = Paths.get(buildLibraryDirectoryPath(library.uuid))
		val exists = Files.exists(newDirectory)
		if (exists && !replaceExisting) {
			val msg = "Library ${library.uuid} already exists"
			LOG.trace(msg)
			throw LibraryImportConflictException(library.uuid)
		}

		if (library.metaGraphCount > quota.maxGraphPerLibrary) {
			val msg = "Only ${quota.maxGraphPerLibrary} graphs per library allowed"
			LOG.trace(msg)
			throw GraphQuotaException(msg, Translations.getString("library.quota.maxGraphPerLibrary.text", quota.maxGraphPerLibrary))
		}

		if (!exists && currentLibraryCount + 1 > quota.maxLibraries) {
			val msg = "Only ${quota.maxLibraries} libraries allowed"
			LOG.trace(msg)
			throw GraphQuotaException(msg, Translations.getString("library.quota.maxLibraries.text", quota.maxLibraries))
		}

		if (exists) {
			FileUtils.deleteDirectory(newDirectory.toFile())
		}
		Files.move(incubationFiles[0].toPath(), newDirectory, StandardCopyOption.REPLACE_EXISTING)

		return library
	}

	override fun buildMetaGraphFilePath(libraryUUID: UUID, metaGraphUuid: UUID): String =
		FileSystems.getDefault().getPath(baseName, libraryUUID.toString(), "$metaGraphUuid.$metaGraphFileExtension").toString()

	private fun buildLibraryFilePath(libraryUuid: UUID): String =
		buildLibraryFilePath(baseName, libraryUuid.toString())

	private fun buildLibraryFilePath(directoryPath: String, libraryDirName: String): String =
		FileSystems.getDefault().getPath(directoryPath, libraryDirName, libraryFileName).toString()

	private fun buildLibraryDirectoryPath(libraryUuid: UUID): String =
		FileSystems.getDefault().getPath(baseName, libraryUuid.toString()).toString()
}
