package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.ResourcesUtil
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.graphics.Image
import ch.scorpion.jabbah.draw.graphics.ImageType
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.MetaGraph
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
* Persists a [Library] in the code's resource directory. Used for system [Libraries][Library].
* @property metaGraphFileExtension the file name extension of [MetaGraph] files
* @property libraryFileName the name of the [Library] file
*/
class ResourceLibraryPersistenceService(
	private val metaGraphFileExtension: String = DEF_META_GRAPH_FILE_EXTENSION,
	private val libraryFileName: String = DEF_LIBRARY_FILE_NAME
) : AbstractFileLibraryPersistenceService() {

	companion object {
		private val LOG by logger(ResourceLibraryPersistenceService::class)
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
		// This is not supported for system Libraries
		throw UnsupportedOperationException("Deleting system Libraries is not supported")
	}

	override fun exportLibrary(libraryId: LibraryIdentification, outputPath: String) {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("Exporting system libraries is not supported")
	}

	override fun exportLibraryTemporarily(libraryId: LibraryIdentification): String {
		val tempDir = Files.createTempDirectory(null)
		val outputLibDir = Path.of(tempDir.toString(), libraryId.uuid.toString()).toAbsolutePath().toString()
		copySystemLibraryFromJarFile(libraryId, outputLibDir)
		return outputLibDir
	}

	// This only works with installed applications, where resource files are packaged in a JAR file
	private fun copySystemLibraryFromJarFile(libraryId: LibraryIdentification, destinationDirectory: String) {
		try {
			val resourcePath = buildResourceLibraryDirectoryPath(libraryId)
			LOG.trace("copy system library $${libraryId.uuid} from JAR resource path $resourcePath to $destinationDirectory")

			// BUG: This doesn't work with obfuscated classes?
			val uri = ResourceLibraryPersistenceService::class.java.getResource(resourcePath)
			LOG.trace("=> URI = $uri")

			val absolutePath = uri.toExternalForm()
			LOG.trace("=> absolute path $absolutePath")

			ResourcesUtil.copyFromJar(absolutePath, Paths.get(destinationDirectory))
		} catch (e: Throwable) {
			LOG.error("Error in copySystemLibraryFromJarFile", e)
			throw e
		}
	}

	override fun importTemporaryLibrary(libraryId: LibraryIdentification, temporaryPath: String) {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("not implemented")
	}

	override suspend fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("Importing system libraries is not supported")
	}

	override fun loadImage(library: Library, imageUuid: UUID, imageType: ImageType): Image =
		DrawModule.imageLoader.loadSystemImage(buildImageFilePath(library.identification, imageUuid, imageType), imageType)

	override fun importImage(library: Library, imageId: ImageIdentification, inputPath: String) {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("not implemented")
	}

	/** ---- [AbstractFileLibraryPersistenceService] */

	override fun buildMetaGraphFilePath(libraryId: LibraryIdentification, metaGraphUuid: UUID): String =
		"${buildResourceLibraryDirectoryPath(libraryId)}/$metaGraphUuid.$metaGraphFileExtension"

	override fun buildImageFilePath(libraryId: LibraryIdentification, imageUuid: UUID, imageType: ImageType): String =
		"${buildResourceLibraryDirectoryPath(libraryId)}/$imageUuid.${imageType.fileExtension}"

	override fun ensureLibraryDirectory(libraryId: LibraryIdentification) {
		val path = Paths.get(buildResourceLibraryDirectoryPath(libraryId))
		if (!Files.exists(path)) {
			Files.createDirectories(path)
		}
	}

	override fun createMetaGraphInputStream(libraryId: LibraryIdentification, metaGraphUuid: UUID): InputStream =
		ResourceLibraryPersistenceService::class.java.getResourceAsStream(buildMetaGraphFilePath(libraryId, metaGraphUuid))

	override fun createMetaGraphOutputStream(libraryId: LibraryIdentification, metaGraphUuid: UUID): OutputStream =
		FileOutputStream(File(ResourceLibraryPersistenceService::class.java.getResource(buildMetaGraphFilePath(libraryId, metaGraphUuid)).toURI()))

	override fun createLibraryFileInputStream(libraryId: LibraryIdentification): InputStream {
		val path = buildLibraryFilePath(libraryId)
		LOG.trace("reading library file from resource $path")
		return ResourceLibraryPersistenceService::class.java.getResourceAsStream(path)
	}

	override fun createLibraryFileOutputStream(libraryId: LibraryIdentification): OutputStream =
		FileOutputStream(File(ResourceLibraryPersistenceService::class.java.getResource(buildLibraryFilePath(libraryId)).toURI()))

	override fun buildLibraryFilePath(libraryId: LibraryIdentification): String =
		"${buildResourceLibraryDirectoryPath(libraryId)}/$libraryFileName"
}