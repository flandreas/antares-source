package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
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
 * An implementation of [LibraryPersistenceService] that stores the libraries in the local file system.
 *
 * @property directoryPath the absolute path of the file system directory where the libraries are located
 */
class FileLibraryPersistenceService(
	private val directoryPath: String,
	private val metaGraphFileExtension: String = "cir",
	private val libraryFileName: String = "library.lib"
) : LibraryPersistenceService {

	companion object {
		private val LOG by logger(FileLibraryPersistenceService::class)
	}

	/** ---- [LibraryPersistenceService] */

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
		LOG.debug("load MetaGraph '$uuid'")
		val filePath = buildMetaGraphFilePath(library.uuid, uuid)
		FileInputStream(filePath).use {
			try {
				val storeReader = StoreXmlReader(ElectricXmlReader(it))
				return storeReader.readStorable() as MetaGraph
			} catch (e: FileNotFoundException) {
				LOG.error("Library file '$filePath' not found")
				throw LibraryPersistenceServiceException()
			} catch (e: Throwable) {
				LOG.error("Error while loading MetaGraph file from '$filePath': ${e.message}")
				throw LibraryPersistenceServiceException()
			}
		}
	}

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
		LOG.debug("store MetaGraph '${metaGraph.uuid}'")
		ensureLibraryDirectory(library.uuid)
		val filePath = buildMetaGraphFilePath(library.uuid, metaGraph.uuid)
		FileOutputStream(filePath).use {
			try {
				val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
				storeWriter.writeStorable(metaGraph)
			} catch (e: Throwable) {
				LOG.error("Error while storing library file to '$filePath': ${e.message}")
				throw e
			}
		}
	}

	override fun deleteContainerLibraryElement(library: Library, uuid: UUID) {
		LOG.debug("delete MetaGraph '$uuid'")
		File(buildMetaGraphFilePath(library.uuid, uuid)).delete()
	}

	override fun loadLibrary(uuid: UUID): Library {
		val path = buildLibraryFilePath(uuid)
		LOG.debug("Loading library from $path")
		FileInputStream(path).use {
			try {
				val storeReader = StoreXmlReader(ElectricXmlReader(it))
				return storeReader.readStorable() as Library
			} catch (e: Throwable) {
				LOG.error("Error while loading library file '$path': ${e.message}")
				throw e
			}
		}
	}

	override fun storeLibrary(library: Library) {
		ensureLibraryDirectory(library.uuid)
		val path = buildLibraryFilePath(library.uuid)
		try {
			FileOutputStream(path).use {
				try {
					val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
					storeWriter.writeStorable(library)
				} catch (e: Throwable) {
					LOG.error("Error while storing library file '$path': ${e.message}")
					throw e
				}
			}
		} catch (e: Throwable) {
			LOG.error("Error while opening library file '$path': ${e.message}")
			throw e
		}
	}

	override fun deleteLibrary(uuid: UUID) {
		LOG.debug("deleteLibrary $uuid")
		FileUtils.deleteDirectory(File(buildLibraryDirectoryPath(uuid)))
	}

	override fun duplicateLibrary(library: Library, newUuid: UUID) {
		LOG.debug("duplicateLibrary ${library.uuid}")
		FileUtils.copyDirectory(
			File(buildLibraryDirectoryPath(library.uuid)),
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

	/** ---- [FileLibraryPersistenceService] */

	private fun buildLibraryDirectoryPath(uuid: UUID): String {
		return FileSystems.getDefault().getPath(directoryPath, uuid.toString()).toString()
	}

	private fun buildMetaGraphFilePath(libraryUUID: UUID, metaGraphUuid: UUID): String {
		return FileSystems.getDefault().getPath(directoryPath, libraryUUID.toString(), "$metaGraphUuid.$metaGraphFileExtension").toString()
	}

	private fun buildLibraryFilePath(uuid: UUID): String {
		return FileSystems.getDefault().getPath(directoryPath, uuid.toString(), libraryFileName).toString()
	}

	private fun ensureLibraryDirectory(uuid: UUID) {
		val path = FileSystems.getDefault().getPath(directoryPath, uuid.toString())
		if (!Files.exists(path)) {
			Files.createDirectory(path)
		}
	}

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