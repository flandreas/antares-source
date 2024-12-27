package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.ZipUtil
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.graphics.ImageType
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphBundle
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.*
import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * An abstract implementation of [LibraryPersistenceService] that stores the libraries in the local file system.
 *
 * @property metaGraphHistoryService the optional [FileMetaGraphHistoryService] used for historizing saved [MetaGraph]s
 */
abstract class AbstractFileLibraryPersistenceService(
	protected val metaGraphHistoryService: FileMetaGraphHistoryService? = null
) : LibraryPersistenceService {

	companion object {
		private val LOG by logger(AbstractFileLibraryPersistenceService::class)
		const val RESOURCE_PATH = "/libraries"
		const val DEF_META_GRAPH_FILE_EXTENSION = "cir"
		const val DEF_LIBRARY_FILE_NAME = "library.xml"

		fun loadLibrary(inputStream: InputStream): Library =
			StoreXmlReader(ElectricXmlReader(inputStream)).readStorable() as Library
	}

	protected abstract fun ensureLibraryDirectory(libraryId: LibraryIdentification)

	protected abstract fun buildMetaGraphFilePath(libraryId: LibraryIdentification, metaGraphUuid: UUID): String

	protected abstract fun buildImageFilePath(libraryId: LibraryIdentification, imageUuid: UUID, imageType: ImageType): String

	@Suppress("unused")
	fun existsMetaGraphFile(libraryId: LibraryIdentification, metaGraphUuid: UUID): Boolean =
		Files.exists(Paths.get(buildMetaGraphFilePath(libraryId, metaGraphUuid)))


	abstract fun createMetaGraphInputStream(libraryId: LibraryIdentification, metaGraphUuid: UUID): InputStream

	abstract fun createMetaGraphOutputStream(libraryId: LibraryIdentification, metaGraphUuid: UUID): OutputStream

	abstract fun createLibraryFileInputStream(libraryId: LibraryIdentification): InputStream

	abstract fun createLibraryFileOutputStream(libraryId: LibraryIdentification): OutputStream

	abstract fun buildLibraryFilePath(libraryId: LibraryIdentification): String

	/** ---- [LibraryPersistenceService] */

	override fun loadMetaGraphXML(library: Library, uuid: UUID): String {
		createMetaGraphInputStream(library.identification, uuid).use {
			return IOUtils.toString(it, Charset.defaultCharset())
		}
	}

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
		createMetaGraphInputStream(library.identification, uuid).use {
			try {
				val storeReader = StoreXmlReader(ElectricXmlReader(it))
				val metaGraph = storeReader.readStorable() as MetaGraph
				LOG.trace("loaded MetaGraph '$uuid' with ID ${metaGraph.hashCode()}")
				return metaGraph
			} catch (e: Throwable) {
				LOG.error("Error while loading MetaGraph $uuid", e)
				throw LibraryPersistenceServiceException()
			}
		}
	}

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
		LOG.trace("store MetaGraph '${metaGraph.uuid} with ID ${metaGraph.hashCode()}'")
		ensureLibraryDirectory(library.identification)

		// Historize if required
		metaGraphHistoryService?.let {
			val filePath = buildMetaGraphFilePath(library.identification, metaGraph.uuid)
			if (File(filePath).exists()) {
				it.historize(library, metaGraph, filePath)
			}
		}

		// Store
		val tempFile = File.createTempFile("metaGraph", null)
		// First write into a temp file so that in case of an exception, the original file doesn't get emptied
		FileOutputStream(tempFile).use {
			try {
				val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
				storeWriter.writeStorable(metaGraph)
				it.flush()
				FileUtils.copyFile(tempFile, File(buildMetaGraphFilePath(library.identification, metaGraph.uuid)))
			} catch (e: Throwable) {
				LOG.error("Error while storing MetaGraph ${metaGraph.uuid}: ${e.message}")
				throw e
			}
		}
	}

	override fun storeLibrary(library: Library) {
		ensureLibraryDirectory(library.identification)
		val tempFile = File.createTempFile("lib", null)
		// First write into a temp file so that in case of an exception, the original file doesn't get emptied
		FileOutputStream(tempFile).use {
			try {
				val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
				storeWriter.writeStorable(library)
				it.flush()
				FileUtils.copyFile(tempFile, File(buildLibraryFilePath(library.identification)))
			} catch (e: Throwable) {
				LOG.error("Error while storing Library ${library.uuid}: ${e.message}")
				throw e
			}
		}
	}

	override fun exportMetaGraphBundle(bundle: MetaGraphBundle, outputPath: String) {
		LOG.trace("Exporting bundle")
		val tempFile = File.createTempFile("bundle", ".cir")

		FileOutputStream(tempFile).use {
			try {
				val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
				storeWriter.writeStorable(bundle)
			} catch (e: Throwable) {
				LOG.error("Error while saving '$tempFile': ${e.message}")
				throw e
			}
		}

		FileOutputStream(outputPath).use { output ->
			ZipOutputStream(output).use {
				ZipUtil.zipFile(tempFile, tempFile.name, it)
			}
		}
	}

	override fun importMetaGraphBundle(inputPath: String): MetaGraphBundle {
		LOG.trace("Importing bundle")

		// Import and unzip to incubation directory
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

		// Load incubating bundle
		val bundleFilePath = FileSystems.getDefault().getPath(incubationDirPath.toAbsolutePath().toString(), incubationFiles[0].name)
		FileInputStream(bundleFilePath.toString()).use { input ->
			try {
				return StoreXmlReader(ElectricXmlReader(input)).readStorable()
			} catch (e: Exception) {
				LOG.trace("Could not read bundle file, possibly not a valid bundle")
				throw IllegalArgumentException("Could not read bundle file")
			}
		}
	}

	/** ---- [AbstractFileLibraryPersistenceService] */

	protected fun loadLibrary(libraryId: LibraryIdentification, inputStream: InputStream): Library {
		try {
			return loadLibrary(inputStream)
		} catch (e: Throwable) {
			LOG.error("Error while loading Library ${libraryId.uuid}: ${e.message}")
			throw LibraryPersistenceServiceException(e.message)
		}
	}

	protected fun buildResourceLibraryDirectoryPath(libraryId: LibraryIdentification): String =
		"$RESOURCE_PATH/${libraryId.uuid.id}"
}