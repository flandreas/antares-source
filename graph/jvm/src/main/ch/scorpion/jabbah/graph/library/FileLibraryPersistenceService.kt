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
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * An implementation of [LibraryPersistenceService] that stores the libraries in the local file system.
 *
 * @property directoryPath the absolute path of the file system directory where the libraries are located
 */
class FileLibraryPersistenceService(
    private val directoryPath: String,
    private val metaGraphFileExtension: String = "cir"
) : LibraryPersistenceService {

    companion object {
        private val LOG by logger(FileLibraryPersistenceService::class)
    }

    /** ---- [LibraryPersistenceService] */

    override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
        LOG.debug("FileLibraryPersistenceService: load MetaGraph '$uuid'")
        val filePath = buildMetaGraphFilePath(uuid)
        FileInputStream(filePath).use {
            try {
                val storeReader = StoreXmlReader(ElectricXmlReader(it))
                return storeReader.readStorable() as MetaGraph
            } catch (e: FileNotFoundException) {
                LOG.error("Library file '$filePath' not found")
                throw LibraryServiceException()
            } catch (e: Throwable) {
                LOG.error("Error while loading MetaGraph file from '$filePath': ${e.message}")
                throw LibraryServiceException()
            }
        }
    }

    override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
        LOG.debug("FileLibraryPersistenceService: store MetaGraph '${metaGraph.uuid}'")
        val filePath = buildMetaGraphFilePath(metaGraph.uuid)
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
        File(buildMetaGraphFilePath(uuid)).delete()
    }

    override fun loadLibrary(library: Library, fileName: String, locationPath: String?) {
        val path = buildLibraryFilePath(locationPath!!, fileName)
        LOG.debug("Loading library from $path")
        FileInputStream(path).use {
            try {
                val storeReader = StoreXmlReader(ElectricXmlReader(it))
                val loadedFolder = storeReader.readStorable() as LibraryFolder
                library.replaceContentsWith(loadedFolder)
            } catch (e: Throwable) {
                LOG.error("Error while loading library file '$path': ${e.message}")
                throw e
            }
        }
    }

    override fun storeLibrary(library: Library, fileName: String, locationPath: String?) {
        val path = buildLibraryFilePath(locationPath!!, fileName)
        try {
            FileOutputStream(path).use {
                try {
                    val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
                    storeWriter.writeStorable(library.libraryFolder)
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

    private fun buildMetaGraphFilePath(uuid: UUID): String {
        return "$directoryPath${System.getProperty("file.separator")}$uuid.$metaGraphFileExtension"
    }

    private fun buildLibraryFilePath(locationPath: String, fileName: String): String {
        return  FileSystems.getDefault().getPath(locationPath, fileName).toString()
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