package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
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
        val filePath = buildMetaGraphFilePath(library.name, uuid)
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
	    ensureLibraryDirectory(library.name)
	    val filePath = buildMetaGraphFilePath(library.name, metaGraph.uuid)
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
        File(buildMetaGraphFilePath(library.name, uuid)).delete()
    }

    override fun loadLibrary(name: String): Library {
	    val path = buildLibraryFilePath(name)
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
	    ensureLibraryDirectory(library.name)
        val path = buildLibraryFilePath(library.name)
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

	override fun deleteLibrary(name: String) {
		LOG.debug("deleteLibrary $name")
		FileUtils.deleteDirectory(File(buildLibraryDirectoryPath(name)))
	}

	override fun duplicateLibrary(library: Library, newName: String) {
		LOG.debug("duplicateLibrary ${library.name}")
		FileUtils.copyDirectory(
			File(buildLibraryDirectoryPath(library.name)),
			File(buildLibraryDirectoryPath(newName))
		)
	}

    override fun exportLibrary(name: String, outputPath: String) {
        LOG.debug("Exporting library to $outputPath")
        FileOutputStream(outputPath).use { output ->
            ZipOutputStream(output).use {
                val fileToZip = File(buildLibraryDirectoryPath(name))
                zipFile(fileToZip, fileToZip.name, it)
            }
        }
    }

	override fun importLibrary(name: String, inputPath: String) {
		LOG.debug("Importing library '$name' from $inputPath")
		FileInputStream(inputPath).use {input ->
			ZipInputStream(input).use {
				unzipFile(Files.createDirectory(Paths.get(name)), it)
			}
		}
	}

	override fun renameLibrary(library: Library, newName: String) {
		LOG.debug("renameLibrary '${library.name}' to '$newName'")
		val oldPath = FileSystems.getDefault().getPath(directoryPath, library.name)
		if (!Files.exists(oldPath)) {
			throw IllegalArgumentException("No directory found for library ${library.name}")
		}
		val newPath = FileSystems.getDefault().getPath(directoryPath, newName)
		Files.move(oldPath, newPath)
	}

    /** ---- [FileLibraryPersistenceService] */

    private fun buildLibraryDirectoryPath(libraryName: String): String {
	    return FileSystems.getDefault().getPath(directoryPath, libraryName).toString()
    }

    private fun buildMetaGraphFilePath(libraryName: String, uuid: UUID): String {
	    return FileSystems.getDefault().getPath(directoryPath, libraryName, "$uuid.$metaGraphFileExtension").toString()
    }

    private fun buildLibraryFilePath(libraryName: String): String {
		return FileSystems.getDefault().getPath(directoryPath, libraryName, libraryFileName).toString()
    }

	private fun ensureLibraryDirectory(libraryName: String) {
		val path = FileSystems.getDefault().getPath(directoryPath, libraryName)
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
            for (childFile in file.listFiles()) {
                zipFile(childFile, "$fileName/${childFile.name}", zipOut)
            }
            return
        }
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