package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.io.ResourcesUtil
import ch.scorpion.jabbah.base.io.ZipUtil
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import org.apache.commons.io.FileUtils
import java.io.*
import java.nio.file.FileSystems
import java.nio.file.Files
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
		LOG.debug("delete MetaGraph $uuid in Library ${library.uuid}")
		File(buildMetaGraphFilePath(library.uuid, uuid)).delete()
	}

	override fun loadLibrary(uuid: UUID): Library {
		LOG.debug("load Library $uuid")
		return createLibraryFileInputStream(uuid).use {
			loadLibrary(uuid, it)
		}
	}

	override fun deleteLibrary(uuid: UUID) {
		LOG.debug("delete Library $uuid")
		FileUtils.deleteDirectory(File(buildLibraryDirectoryPath(uuid)))
	}

	override fun duplicateLibrary(library: Library, newUuid: UUID) {
		val destinationDirectory = buildLibraryDirectoryPath(newUuid)

		if (library.isSystem) {
			try {
				copySystemLibraryFromJarFile(library.uuid, destinationDirectory)
			} catch (e: Throwable) {
				LOG.debug("Could not copy library from JAR file, trying resources in file system")
				copySystemLibraryFromFileSystem(library.uuid, destinationDirectory)
			}
		} else {
			copyUserLibrary(library.uuid, destinationDirectory)
		}
	}

	// This only works in the development environment, where resource files lie in the file system
	private fun copySystemLibraryFromFileSystem(uuid: UUID, destinationDirectory: String) {
		val resourcePath = buildResourceLibraryDirectoryPath(uuid)
		LOG.debug("duplicate system library $uuid from file system resource path $resourcePath")
		val absolutePath = File(FileLibraryPersistenceService::class.java.getResource(resourcePath).toURI()).absolutePath
		LOG.debug("=> absolute path $absolutePath")

		copyLibrary(absolutePath, destinationDirectory)
	}

	// This only works with installed applications, where resource files are packages in a JAR file
	private fun copySystemLibraryFromJarFile(uuid: UUID, destinationDirectory: String) {
		val resourcePath = buildResourceLibraryDirectoryPath(uuid)
		LOG.debug("copy system library $uuid from JAR resource path $resourcePath")
		val absolutePath = FileLibraryPersistenceService::class.java.getResource(resourcePath).toExternalForm()
		LOG.debug("=> absolute path $absolutePath")
		ResourcesUtil.copyFromJar(absolutePath, Paths.get(destinationDirectory))
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
		LOG.debug("Exporting library to $outputPath")
		FileOutputStream(outputPath).use { output ->
			ZipOutputStream(output).use {
				val fileToZip = File(buildLibraryDirectoryPath(uuid))
				ZipUtil.zipFile(fileToZip, fileToZip.name, it)
			}
		}
	}

	override fun importLibrary(inputPath: String): UUID {
		LOG.debug("Importing library from $inputPath")


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
			LOG.debug(msg)
			throw IllegalArgumentException(msg)
		}

		// Load incubating Library
		val libraryFilePath = buildLibraryFilePath(incubationDirPath.toAbsolutePath().toString(), incubationFiles[0].name)
		val library = createLibraryFileInputStream(libraryFilePath).use {
			try {
				loadLibrary(it)
			} catch (e: Exception) {
				LOG.debug("Could not read library file, possibly not an Antares library")
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
		LOG.debug("delete MetaGraph $uuid in Library ${library.uuid}")
		File(buildMetaGraphFilePath(library.uuid, uuid)).delete()
	}

	override fun loadLibrary(uuid: UUID): Library {
		LOG.debug("load Library $uuid")
		return createLibraryFileInputStream(uuid).use {
			loadLibrary(uuid, it)
		}
	}

	override fun deleteLibrary(uuid: UUID) {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("Deleting system Libraries is not supported")
	}

	override fun duplicateLibrary(library: Library, newUuid: UUID) {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("System Libraries cannot be the target of duplication")
	}

	override fun exportLibrary(uuid: UUID, outputPath: String) {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("Exporting system libraries is not supported")
	}

	override fun importLibrary(inputPath: String): UUID {
		// This is not supported for system Libraries
		throw UnsupportedOperationException("Importing system libraries is not supported")
	}

	/** ---- [AbstractFileLibraryPersistenceService] */

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

	override fun createLibraryFileInputStream(libraryUuid: UUID): InputStream =
		ResourceLibraryPersistenceService::class.java.getResourceAsStream(buildLibraryFilePath(libraryUuid))

	override fun createLibraryFileOutputStream(libraryUuid: UUID): OutputStream =
		FileOutputStream(File(ResourceLibraryPersistenceService::class.java.getResource(buildLibraryFilePath(libraryUuid)).toURI()))

	/** ---- [ResourceLibraryPersistenceService] */

	private fun buildMetaGraphFilePath(libraryUuid: UUID, metaGraphUuid: UUID): String =
		"${buildResourceLibraryDirectoryPath(libraryUuid)}/$metaGraphUuid.$metaGraphFileExtension"

	private fun buildLibraryFilePath(libraryUuid: UUID): String =
		"${buildResourceLibraryDirectoryPath(libraryUuid)}/$libraryFileName"
}

/**
 * An abstract implementation of [LibraryPersistenceService] that stores the libraries in the local file system.
 */
abstract class AbstractFileLibraryPersistenceService : LibraryPersistenceService {

	companion object {
		private val LOG by logger(AbstractFileLibraryPersistenceService::class)
		const val RESOURCE_PATH = "/libraries"
		const val DEF_META_GRAPH_FILE_EXTENSION = "cir"
		const val DEF_LIBRARY_FILE_NAME = "library.xml"
	}

	protected abstract fun ensureLibraryDirectory(libraryUuid: UUID)

	protected abstract fun createMetaGraphInputStream(libraryUuid: UUID, metaGraphUuid: UUID): InputStream

	protected abstract fun createMetaGraphOutputStream(libraryUuid: UUID, metaGraphUuid: UUID): OutputStream

	protected abstract fun createLibraryFileInputStream(libraryUuid: UUID): InputStream

	protected abstract fun createLibraryFileOutputStream(libraryUuid: UUID): OutputStream

	/** ---- [LibraryPersistenceService] */

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
		LOG.debug("load MetaGraph '$uuid'")
		createMetaGraphInputStream(library.uuid, uuid).use {
			try {
				val storeReader = StoreXmlReader(ElectricXmlReader(it))
				return storeReader.readStorable() as MetaGraph
			} catch (e: Throwable) {
				LOG.error("Error while loading MetaGraph $uuid: ${e.message}")
				throw LibraryPersistenceServiceException()
			}
		}
	}

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
		LOG.debug("store MetaGraph '${metaGraph.uuid}'")
		ensureLibraryDirectory(library.uuid)
		createMetaGraphOutputStream(library.uuid, metaGraph.uuid).use {
			try {
				val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
				storeWriter.writeStorable(metaGraph)
			} catch (e: Throwable) {
				LOG.error("Error while storing MetaGraph ${metaGraph.uuid}: ${e.message}")
				throw e
			}
		}
	}

	override fun storeLibrary(library: Library) {
		ensureLibraryDirectory(library.uuid)
		createLibraryFileOutputStream(library.uuid).use {
			try {
				val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
				storeWriter.writeStorable(library)
			} catch (e: Throwable) {
				LOG.error("Error while storing Library ${library.uuid}: ${e.message}")
				throw e
			}
		}
	}

	/** ---- [AbstractFileLibraryPersistenceService] */

	protected fun loadLibrary(uuid: UUID, inputStream: InputStream): Library {
		try {
			return loadLibrary(inputStream)
		} catch (e: Throwable) {
			LOG.error("Error while loading Library $uuid: ${e.message}")
			throw LibraryPersistenceServiceException(e.message)
		}
	}

	protected fun loadLibrary(inputStream: InputStream): Library =
		StoreXmlReader(ElectricXmlReader(inputStream)).readStorable() as Library

	protected fun buildResourceLibraryDirectoryPath(libraryUuid: UUID): String =
		"$RESOURCE_PATH/$libraryUuid"
}