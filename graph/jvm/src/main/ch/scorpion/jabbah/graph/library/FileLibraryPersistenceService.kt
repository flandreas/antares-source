package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.ZipUtil
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.graphics.Image
import ch.scorpion.jabbah.draw.graphics.ImageType
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.GraphQuotaException
import ch.scorpion.jabbah.graph.MetaGraph
import org.apache.commons.io.FileUtils
import java.io.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipOutputStream

/**
 * Persists a [Library] in a user's local file system directory.
 *
 * @property baseDirectoryProvider provides the path of the base data directory
 * @property directoryName the name of the directory within [baseDirectoryProvider] that holds the [Library] files (e.g. "libraries")
 * @property metaGraphFileExtension the file name extension of [MetaGraph] files
 * @property libraryFileName the name of the [Library] file
 * @property metaGraphHistoryService the optional [FileMetaGraphHistoryService] used for historizing saved [MetaGraph]s
 */
@Suppress("MemberVisibilityCanBePrivate")
class FileLibraryPersistenceService(
	private val baseDirectoryProvider: () -> String,
	private val directoryName: String?,
	private val metaGraphFileExtension: String = DEF_META_GRAPH_FILE_EXTENSION,
	private val libraryFileName: String = DEF_LIBRARY_FILE_NAME,
	metaGraphHistoryService: FileMetaGraphHistoryService? = null
) : AbstractFileLibraryPersistenceService(metaGraphHistoryService) {

	companion object {
		private val LOG by logger(FileLibraryPersistenceService::class)

		fun buildLibraryFilePath(directoryPath: String, libraryDirName: String, libraryFileName: String): String =
			FileSystems.getDefault().getPath(directoryPath, libraryDirName, libraryFileName).toString()
	}

	/** ---- [LibraryPersistenceService] */

	override fun deleteMetaGraph(library: Library, uuid: UUID) {
		LOG.trace("delete MetaGraph $uuid in Library ${library.uuid}")
		metaGraphHistoryService?.deleteHistory(uuid)
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

	override suspend fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library {
		LOG.trace("Importing library from $inputPath")
		return importLibrary(FileInputStream(inputPath), replaceExisting = false, currentLibraryCount, quota)
	}

	override fun loadImage(library: Library, imageUuid: UUID, imageType: ImageType): Image {
		try {
			val image = DrawModule.imageLoader.loadUserImage(
				buildImageFilePath(
					library.identification,
					imageUuid,
					imageType
				),
				imageType
			)
			LOG.trace("Loaded image $imageUuid")
			return image
		} catch (e: Throwable) {
			throw LibraryPersistenceServiceException()
		}
	}

	override fun importImage(library: Library, imageId: ImageIdentification, inputPath: String) {
		val outputPath = buildImageFilePath(library.identification, imageId.uuid, imageId.imageType)
		FileUtils.copyFile(File(inputPath), File(outputPath))
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

	private fun getBaseName(): String {
		val separator = FileSystems.getDefault().separator
		val dataPath = baseDirectoryProvider()
		return if (directoryName == null) {
			dataPath
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
	suspend fun importLibrary(
		inputStream: InputStream,
		replaceExisting: Boolean,
		currentLibraryCount: Int,
		quota: GraphQuota = GraphQuota.UNLIMITED
	): Library = FileLibraryImporter(this).import(
		inputStream, replaceExisting, currentLibraryCount, quota)

	override fun buildMetaGraphFilePath(libraryId: LibraryIdentification, metaGraphUuid: UUID): String =
		FileSystems.getDefault().getPath(getBaseName(), libraryId.uuid.toString(), "$metaGraphUuid.$metaGraphFileExtension").toString()

	override fun buildImageFilePath(libraryId: LibraryIdentification, imageUuid: UUID, imageType: ImageType): String =
		FileSystems.getDefault().getPath(getBaseName(), libraryId.uuid.toString(), "$imageUuid.${imageType.fileExtension}").toString()

	private fun buildLibraryFilePath(libraryId: LibraryIdentification): String =
		buildLibraryFilePath(getBaseName(), libraryId.uuid.toString())

	private fun buildLibraryFilePath(directoryPath: String, libraryDirName: String): String =
		FileSystems.getDefault().getPath(directoryPath, libraryDirName, libraryFileName).toString()

	fun buildLibraryDirectoryPath(libraryId: LibraryIdentification): String =
		FileSystems.getDefault().getPath(getBaseName(), libraryId.uuid.toString()).toString()
}
