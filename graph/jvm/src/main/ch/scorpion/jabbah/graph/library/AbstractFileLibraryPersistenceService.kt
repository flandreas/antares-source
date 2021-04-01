package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import java.io.InputStream
import java.io.OutputStream

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
		createMetaGraphInputStream(library.uuid, uuid).use {
			try {
				val storeReader = StoreXmlReader(ElectricXmlReader(it))
				val metaGraph = storeReader.readStorable() as MetaGraph
				LOG.debug("loaded MetaGraph '$uuid' with ID ${metaGraph.hashCode()}")
				return metaGraph
			} catch (e: Throwable) {
				LOG.error("Error while loading MetaGraph $uuid: ${e.message}")
				throw LibraryPersistenceServiceException()
			}
		}
	}

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
		LOG.debug("store MetaGraph '${metaGraph.uuid} with ID ${metaGraph.hashCode()}'")
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