package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.ResourcesUtil
import ch.scorpion.jabbah.base.logger
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
		File(buildMetaGraphFilePath(library.uuid, uuid)).delete()
	}

	override fun loadLibrary(uuid: UUID): Library {
		LOG.trace("load Library $uuid")
		return createLibraryFileInputStream(uuid).use {
			loadLibrary(uuid, it)
		}
	}

	override fun deleteLibrary(uuid: UUID) {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("Deleting system Libraries is not supported")
	}

	override fun exportLibrary(uuid: UUID, outputPath: String) {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("Exporting system libraries is not supported")
	}

	override fun exportLibraryTemporarily(uuid: UUID): String {
		val tempDir = Files.createTempDirectory(null)
		val outputLibDir = Path.of(tempDir.toString(), uuid.toString()).toAbsolutePath().toString()
		copySystemLibraryFromJarFile(uuid, outputLibDir)
		return outputLibDir
	}

	// This only works with installed applications, where resource files are packaged in a JAR file
	private fun copySystemLibraryFromJarFile(uuid: UUID, destinationDirectory: String) {
		try {
			val resourcePath = buildResourceLibraryDirectoryPath(uuid)
			LOG.trace("copy system library $uuid from JAR resource path $resourcePath to $destinationDirectory")

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

	override fun importTemporaryLibrary(uuid: UUID, temporaryPath: String) {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("not implemented")
	}

	override fun importLibrary(inputPath: String): UUID {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("Importing system libraries is not supported")
	}

	/** ---- [AbstractFileLibraryPersistenceService] */

	override fun buildMetaGraphFilePath(libraryUuid: UUID, metaGraphUuid: UUID): String =
		"${buildResourceLibraryDirectoryPath(libraryUuid)}/$metaGraphUuid.$metaGraphFileExtension"

	override fun ensureLibraryDirectory(libraryUuid: UUID) {
		val path = Paths.get(buildResourceLibraryDirectoryPath(libraryUuid))
		if (!Files.exists(path)) {
			Files.createDirectories(path)
		}
	}

	override fun createMetaGraphInputStream(libraryUuid: UUID, metaGraphUuid: UUID): InputStream =
		ResourceLibraryPersistenceService::class.java.getResourceAsStream(buildMetaGraphFilePath(libraryUuid, metaGraphUuid))

	override fun createMetaGraphOutputStream(libraryUuid: UUID, metaGraphUuid: UUID): OutputStream =
		FileOutputStream(File(ResourceLibraryPersistenceService::class.java.getResource(buildMetaGraphFilePath(libraryUuid, metaGraphUuid)).toURI()))

	override fun createLibraryFileInputStream(libraryUuid: UUID): InputStream {
		val path = buildLibraryFilePath(libraryUuid)
		LOG.trace("reading library file from resource $path")
		return ResourceLibraryPersistenceService::class.java.getResourceAsStream(path)
	}

	override fun createLibraryFileOutputStream(libraryUuid: UUID): OutputStream =
		FileOutputStream(File(ResourceLibraryPersistenceService::class.java.getResource(buildLibraryFilePath(libraryUuid)).toURI()))

	/** ---- [ResourceLibraryPersistenceService] */

	private fun buildLibraryFilePath(libraryUuid: UUID): String =
		"${buildResourceLibraryDirectoryPath(libraryUuid)}/$libraryFileName"
}