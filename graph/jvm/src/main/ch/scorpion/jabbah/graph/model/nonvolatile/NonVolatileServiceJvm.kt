package ch.scorpion.jabbah.graph.model.nonvolatile

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

/**
 * Persists non-volatile [Graph] data in a user's local file system directory.
 */
class NonVolatileServiceJvm(
    private val baseDirectoryProvider: () -> String,
    private val directoryName: String?
) : NonVolatileService {

    companion object {
        private val LOG by logger(NonVolatileServiceJvm::class)
    }

    override fun load(rootMetaGraphId: UUID): NonVolatileStorable? {
        val path = buildStorableFilePath(rootMetaGraphId)
        if (!Files.exists(path)) {
            return null
        }

        ensureDirectory()
        FileInputStream(path.toFile()).use {
            try {
                val storeReader = StoreXmlReader(ElectricXmlReader(it))
                val storable = storeReader.readStorable() as NonVolatileStorable
                return storable
            } catch (e: Throwable) {
                LOG.error("Error while loading non-volatile data for ${rootMetaGraphId.id}: ${e.message}")
                throw e
            }
        }
    }

    override fun store(rootMetaGraphId: UUID, nonVolatileStorable: NonVolatileStorable) {
        ensureDirectory()
        createStorableFileOutputStream(rootMetaGraphId).use {
            try {
                val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
                storeWriter.writeStorable(nonVolatileStorable)
            } catch (e: Throwable) {
                LOG.error("Error while storing non-volatile data for ${rootMetaGraphId.id}: ${e.message}")
                throw e
            }
        }
    }

    override fun delete(rootMetaGraphId: UUID) {
        val path = buildStorableFilePath(rootMetaGraphId)
        if (Files.exists(path)) {
            Files.delete(path)
        }
    }

    private fun ensureDirectory() {
        val path = buildDirectoryPath()
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
    }

    private fun buildDirectoryPath(): Path = FileSystems.getDefault().getPath(getBaseName())

    private fun getBaseName(): String {
        val separator = FileSystems.getDefault().separator
        val dataPath = baseDirectoryProvider()
        return if (directoryName == null) {
            dataPath
        } else {
            "$dataPath$separator$directoryName"
        }
    }

    private fun createStorableFileOutputStream(rootMetaGraphId: UUID): FileOutputStream {
        val path = buildStorableFilePath(rootMetaGraphId)
        try {
            return FileOutputStream(path.toFile())
        } catch (e: FileNotFoundException) {
            LOG.error("Non-volatile file $path not found")
            throw e
        }
    }

    private fun buildStorableFilePath(rootMetaGraphId: UUID): Path =
        FileSystems.getDefault().getPath(getBaseName(), rootMetaGraphId.toString())
}