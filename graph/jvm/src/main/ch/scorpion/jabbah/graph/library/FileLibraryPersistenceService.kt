package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.zip.ZipEntry
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
        LOG.debug("FileLibraryPersistenceService: load MetaGraph '$uuid'")
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
        LOG.debug("FileLibraryPersistenceService: store MetaGraph '${metaGraph.uuid}'")
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
        LOG.debug("FileLibraryPersistenceService: delete MetaGraph '$uuid'")
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

    override fun exportLibrary(fileName: String, locationPath: String?) {
        val path = FileSystems.getDefault().getPath(locationPath, fileName).toString()
        LOG.debug("Exporting library to $path")
        FileOutputStream(path).use { output ->
            ZipOutputStream(output).use {
                val fileToZip = File(directoryPath)
                zipFile(fileToZip, fileToZip.name, it)
            }
        }
    }

    /** ---- [FileLibraryPersistenceService] */

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
            val buffer = ByteArray(1024, { 0 })
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