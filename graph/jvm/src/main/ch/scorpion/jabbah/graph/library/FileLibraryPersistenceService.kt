package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
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
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
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

	override fun deleteContainerLibraryElement(library: Library, uuid: UUID) {
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
		FileUtils.deleteDirectory(File(buildLibraryDirectoryPath(uuid).toString()))
	}

	override fun duplicateLibrary(library: Library, newUuid: UUID) {
		LOG.debug("duplicateLibrary ${library.uuid}")
		val sourceDirectory = if (library.isSystem) {
			File(FileLibraryPersistenceService::class.java.getResource(buildResourceLibraryDirectoryPath(library.uuid)).toURI()).absolutePath
		} else {
			buildLibraryDirectoryPath(library.uuid)
		}

		FileUtils.copyDirectory(
			File(sourceDirectory),
			File(buildLibraryDirectoryPath(newUuid))
		)
	}

	override fun exportLibrary(uuid: UUID, outputPath: String) {
		LOG.debug("Exporting library to $outputPath")
		FileOutputStream(outputPath).use { output ->
			ZipOutputStream(output).use {
				val fileToZip = File(buildLibraryDirectoryPath(uuid))
				zipFile(fileToZip, fileToZip.name, it)
			}
		}
	}

	override fun importLibrary(uuid: UUID, inputPath: String) {
		LOG.debug("Importing library '$uuid' from $inputPath")
		Files.createDirectory(Paths.get(buildLibraryDirectoryPath(uuid)))
		FileInputStream(inputPath).use { input ->
			ZipInputStream(input).use {
				unzipFile(Paths.get(directoryPath), it)
			}
		}
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

	override fun createLibraryFileInputStream(libraryUuid: UUID): InputStream {
		val path = buildLibraryFilePath(libraryUuid)
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

	private fun buildLibraryFilePath(libraryUuid: UUID): String =
		FileSystems.getDefault().getPath(directoryPath, libraryUuid.toString(), libraryFileName).toString()

	private fun buildLibraryDirectoryPath(libraryUuid: UUID): String =
		FileSystems.getDefault().getPath(directoryPath, libraryUuid.toString()).toString()

	private fun zipFile(file: File, fileName: String, zipOut: ZipOutputStream) {
		if (file.isHidden) {
			return
		}
		LOG.debug(".. zipping $fileName")
		if (file.isDirectory) {
			file.listFiles()?.forEach { zipFile(it, "$fileName/${it.name}", zipOut) }
		} else {
			FileInputStream(file).use {
				val zipEntry = ZipEntry(fileName)
				zipOut.putNextEntry(zipEntry)
				val buffer = ByteArray(1024) { 0 }
				var length: Int
				do {
					length = it.read(buffer)
					if (length > 0) {
						zipOut.write(buffer, 0, length)
					}
				} while (length > 0)
			}
		}
	}

	private fun unzipFile(destDir: Path, zipIn: ZipInputStream) {
		val buffer = ByteArray(1024) { 0 }
		var zipEntry = zipIn.nextEntry
		while (zipEntry != null) {
			val newFile = newFile(destDir.toFile(), zipEntry)
			FileOutputStream(newFile).use {
				var length: Int
				do {
					length = zipIn.read(buffer)
					if (length > 0) {
						it.write(buffer, 0, length)
					}

				} while (length > 0)
			}
			zipEntry = zipIn.nextEntry
		}
	}

	/**
	 * Returns the destination [File] of the specified [ZipEntry] by checking that it is a
	 * subdirectory of the overall directory, hereby guarding against the "Zip Slip" vulnerability.
	 * See https://www.baeldung.com/java-compress-and-uncompress.
	 */
	private fun newFile(destDir: File, zipEntry: ZipEntry): File {
		val destFile = File(destDir, zipEntry.name)
		val destDirPath = destDir.canonicalPath
		val destFilePath = destFile.canonicalPath

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw IOException("Entry is outside of the target dir: ${zipEntry.name}")
		}

		return destFile
	}
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

	override fun deleteContainerLibraryElement(library: Library, uuid: UUID) {
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

	override fun importLibrary(uuid: UUID, inputPath: String) {
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
			val storeReader = StoreXmlReader(ElectricXmlReader(inputStream))
			return storeReader.readStorable() as Library
		} catch (e: Throwable) {
			LOG.error("Error while loading Library $uuid: ${e.message}")
			throw LibraryPersistenceServiceException(e.message)
		}
	}

	protected fun buildResourceLibraryDirectoryPath(libraryUuid: UUID): String =
		"$RESOURCE_PATH/$libraryUuid"
}