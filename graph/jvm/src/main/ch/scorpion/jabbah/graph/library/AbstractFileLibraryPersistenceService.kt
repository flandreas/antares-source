package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.ZipUtil
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphBundle
import ch.scorpion.jabbah.io.*
import java.io.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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

	protected abstract fun ensureLibraryDirectory(libraryId: LibraryIdentification)

	protected abstract fun buildMetaGraphFilePath(libraryId: LibraryIdentification, metaGraphUuid: UUID): String

	@Suppress("unused")
	fun existsMetaGraphFile(libraryId: LibraryIdentification, metaGraphUuid: UUID): Boolean =
		Files.exists(Paths.get(buildMetaGraphFilePath(libraryId, metaGraphUuid)))


	abstract fun createMetaGraphInputStream(libraryId: LibraryIdentification, metaGraphUuid: UUID): InputStream

	abstract fun createMetaGraphOutputStream(libraryId: LibraryIdentification, metaGraphUuid: UUID): OutputStream

	abstract fun createLibraryFileInputStream(libraryId: LibraryIdentification): InputStream

	abstract fun createLibraryFileOutputStream(libraryId: LibraryIdentification): OutputStream

	/** ---- [LibraryPersistenceService] */

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
		createMetaGraphInputStream(library.identification, uuid).use {
			try {
				val storeReader = StoreXmlReader(ElectricXmlReader(it))
				val metaGraph = storeReader.readStorable() as MetaGraph
				LOG.trace("loaded MetaGraph '$uuid' with ID ${metaGraph.hashCode()}")
				return metaGraph
			} catch (e: Throwable) {
				LOG.error("Error while loading MetaGraph $uuid: ${e.message}")
				throw LibraryPersistenceServiceException()
			}
		}
	}

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
		LOG.trace("store MetaGraph '${metaGraph.uuid} with ID ${metaGraph.hashCode()}'")
		ensureLibraryDirectory(library.identification)
		createMetaGraphOutputStream(library.identification, metaGraph.uuid).use {
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
		ensureLibraryDirectory(library.identification)
		createLibraryFileOutputStream(library.identification).use {
			try {
				val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
				storeWriter.writeStorable(library)
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
			LOG.error("Error while loading Library $${libraryId.uuid}: ${e.message}")
			throw LibraryPersistenceServiceException(e.message)
		}
	}

	protected fun loadLibrary(inputStream: InputStream): Library =
		StoreXmlReader(ElectricXmlReader(inputStream)).readStorable() as Library

	protected fun buildResourceLibraryDirectoryPath(libraryId: LibraryIdentification): String =
		"$RESOURCE_PATH/${libraryId.uuid.id}"
}