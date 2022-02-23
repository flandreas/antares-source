package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.ZipUtil
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.UserIdentity
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
 * @property useOwner if `true`, [LibraryIdentification.owner] is part of the file system path. Typically `false`
 * for single-user environments, `true` for multi-user environments
 */
@Suppress("MemberVisibilityCanBePrivate")
class FileLibraryPersistenceService(
	private val dataPath: String,
	private val directoryName: String,
	private val metaGraphFileExtension: String = DEF_META_GRAPH_FILE_EXTENSION,
	private val libraryFileName: String = DEF_LIBRARY_FILE_NAME,
	private val useOwner: Boolean = false
) : AbstractFileLibraryPersistenceService() {

	companion object {
		private val LOG by logger(FileLibraryPersistenceService::class)
	}

	/** ---- [LibraryPersistenceService] */

	override fun deleteMetaGraph(library: Library, uuid: UUID) {
		LOG.trace("delete MetaGraph $uuid in Library ${library.uuid}")
		File(buildMetaGraphFilePath(library.identification, uuid)).delete()
	}

	override fun loadLibrary(libraryId: LibraryIdentification): Library {
		LOG.trace("load Library $${libraryId.uuid}")
		return createLibraryFileInputStream(libraryId).use {
			loadLibrary(libraryId, it)
		}
	}

	override fun deleteLibrary(libraryId: LibraryIdentification) {
		LOG.trace("delete Library $${libraryId.uuid}")
		FileUtils.deleteDirectory(File(buildLibraryDirectoryPath(libraryId)))
	}

	private fun copyUserLibrary(libraryId: LibraryIdentification, destinationDirectory: String) {
		val sourceDirectory = buildLibraryDirectoryPath(libraryId)
		copyLibrary(sourceDirectory, destinationDirectory)
	}

	private fun copyLibrary(sourceDirectory: String, destinationDirectory: String) {
		FileUtils.copyDirectory(
			File(sourceDirectory),
			File(destinationDirectory)
		)
	}

	override fun exportLibrary(libraryId: LibraryIdentification, outputPath: String) {
		LOG.trace("Exporting library ${libraryId.uuid} to $outputPath")
		exportLibrary(libraryId, FileOutputStream(outputPath))
	}

	fun exportLibrary(libraryId: LibraryIdentification, outputStream: FileOutputStream) {
		outputStream.use { output ->
			ZipOutputStream(output).use {
				val fileToZip = File(buildLibraryDirectoryPath(libraryId))
				ZipUtil.zipFile(fileToZip, fileToZip.name, it)
			}
		}
	}

	override fun exportLibraryTemporarily(libraryId: LibraryIdentification): String {
		val tempDir = Files.createTempDirectory(null)
		val outputLibDir = Path.of(tempDir.toString(), libraryId.uuid.toString()).toAbsolutePath().toString()
		copyUserLibrary(libraryId, outputLibDir)

		return outputLibDir
	}

	override fun importTemporaryLibrary(libraryId: LibraryIdentification, temporaryPath: String) {
		copyLibrary(temporaryPath, buildLibraryDirectoryPath(libraryId))
	}

	override fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library {
		LOG.trace("Importing library from $inputPath")
		return importLibrary(FileInputStream(inputPath), replaceExisting = false, currentLibraryCount, quota)
	}

	/** ---- [AbstractFileLibraryPersistenceService] */

	override fun ensureLibraryDirectory(libraryId: LibraryIdentification) {
		val path = Paths.get(buildLibraryDirectoryPath(libraryId))
		if (!Files.exists(path)) {
			Files.createDirectories(path)
		}
	}

	override fun createMetaGraphInputStream(libraryId: LibraryIdentification, metaGraphUuid: UUID): InputStream {
		val path = buildMetaGraphFilePath(libraryId, metaGraphUuid)
		try {
			return FileInputStream(path)
		} catch (e: FileNotFoundException) {
			LOG.error("MetaGraph file $path not found")
			throw e
		}
	}

	override fun createMetaGraphOutputStream(libraryId: LibraryIdentification, metaGraphUuid: UUID): OutputStream {
		val path = buildMetaGraphFilePath(libraryId, metaGraphUuid)
		try {
			return FileOutputStream(path)
		} catch (e: FileNotFoundException) {
			LOG.error("MetaGraph file $path not found")
			throw e
		}
	}

	override fun createLibraryFileInputStream(libraryId: LibraryIdentification): InputStream =
		createLibraryFileInputStream(buildLibraryFilePath(libraryId))

	private fun createLibraryFileInputStream(path: String): InputStream {
		try {
			return FileInputStream(path)
		} catch (e: FileNotFoundException) {
			LOG.error("Library file $path not found")
			throw e
		}
	}

	override fun createLibraryFileOutputStream(libraryId: LibraryIdentification): OutputStream {
		val path = buildLibraryFilePath(libraryId)
		try {
			return FileOutputStream(path)
		} catch (e: FileNotFoundException) {
			LOG.error("Library file $path not found")
			throw e
		}
	}

	/** ---- [FileLibraryPersistenceService] */

	private fun getBaseName(owner: UserIdentity?): String {
		val separator = FileSystems.getDefault().separator

		return if (useOwner && owner != null) {
			"$dataPath$separator${owner.id}$separator$directoryName"
		} else {
			"$dataPath$separator$directoryName"
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
		val newDirectory = Paths.get(buildLibraryDirectoryPath(library.identification))
		val exists = Files.exists(newDirectory)
		if (exists && !replaceExisting) {
			val msg = "Library ${library.uuid} already exists"
			LOG.trace(msg)
			throw LibraryImportConflictException(library.identification)
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

	override fun buildMetaGraphFilePath(libraryId: LibraryIdentification, metaGraphUuid: UUID): String =
		FileSystems.getDefault().getPath(getBaseName(libraryId.owner), libraryId.uuid.toString(), "$metaGraphUuid.$metaGraphFileExtension").toString()

	private fun buildLibraryFilePath(libraryId: LibraryIdentification): String =
		buildLibraryFilePath(getBaseName(libraryId.owner), libraryId.uuid.toString())

	private fun buildLibraryFilePath(directoryPath: String, libraryDirName: String): String =
		FileSystems.getDefault().getPath(directoryPath, libraryDirName, libraryFileName).toString()

	private fun buildLibraryDirectoryPath(libraryId: LibraryIdentification): String =
		FileSystems.getDefault().getPath(getBaseName(libraryId.owner), libraryId.uuid.toString()).toString()
}
