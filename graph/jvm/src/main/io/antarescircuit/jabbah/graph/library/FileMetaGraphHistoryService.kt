package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.app.module.AppModuleJvm
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.io.ElectricXmlReader
import io.antarescircuit.jabbah.io.StoreXmlReader
import org.apache.commons.io.FileUtils
import java.io.FileInputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

/**
 * Service for creating and managing the save history of [MetaGraphs][MetaGraph].
 */
interface FileMetaGraphHistoryService {

	companion object {
		/** The name of the [Boolean] property in [Properties] defining whether [MetaGraph] historizing is enabled. */
		const val PREF_META_GRAPH_HISTORY = "graph.library.metaGraphHistory"
	}

	/**
	 * Stores a history snapshot of [metaGraph] whose original to be copied is stored at [sourceFilePath].
	 */
	fun historize(library: Library, metaGraph: MetaGraph, sourceFilePath: String)

	/**
	 * Returns the [MetaGraphHistory] items of a [MetaGraph] with [UUID] [metaGraphUuid].
	 * The returned list is sorted descending by [MetaGraphHistory.timestamp].
	 */
	fun getHistory(metaGraphUuid: UUID): List<MetaGraphHistory>

	/**
	 * Loads a historized [MetaGraph].
	 */
	fun getMetaGraph(library: Library, metaGraphUuid: UUID, history: MetaGraphHistory): MetaGraph

	/**
	 * Deletes all [MetaGraphHistory] entries for the [MetaGraph] with the specified [UUID].
	 */
	fun deleteHistory(metaGraphUuid: UUID)
}

/**
 * Represents a single entry in the save history of a [MetaGraph].
 * @property fileName the physical name of the file in which the entry is stored
 * @property timestamp the timestamp of when this entry was created.
 */
data class MetaGraphHistory(
	val fileName: String,
	val timestamp: LocalDateTime
) : Comparable<MetaGraphHistory> {

	override fun compareTo(other: MetaGraphHistory): Int = this.timestamp.compareTo(other.timestamp)
}

/**
 * Service for storing historized versions of [MetaGraph]s.
 *
 * Used in conjunction with [FileLibraryPersistenceService] that supports
 * historizing [MetaGraph]s.
 *
 * Storage structure:
 * <pre>
 * /{dataPath}
 *    /{directoryName}
 *        /{metaGraphUuid}
 *            yyyy-MM-dd-HH-mm-ss-xxx
 *        /{metaGraphUuid}
 *            yyyy-MM-dd-HH-mm-ss-xxx
 * </pre>
 *
 * @property baseDirectoryProvider provides the path of the base data directory
 * @property directoryName the name of the directory within the current [Workspace] holding the history files, e.g. "history"
 */
class FileMetaGraphHistoryServiceImpl(
	private val baseDirectoryProvider: () -> String,
	private val directoryName: String = "history"
) : FileMetaGraphHistoryService {

	companion object {
		private val LOG by logger(FileMetaGraphHistoryService::class)
		private val FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS")
	}

	private val historyDirectoryPath: Path get() = FileSystems.getDefault().getPath(baseDirectoryProvider(), directoryName)

	/** ---- [FileMetaGraphHistoryService] */

	override fun historize(library: Library, metaGraph: MetaGraph, sourceFilePath: String) {
		if (!BaseModule.properties.getBoolean(FileMetaGraphHistoryService.PREF_META_GRAPH_HISTORY)) {
			return
		}

		LOG.trace("Historizing MetaGraph ${metaGraph.uuid}")

		ensureDirectory(historyDirectoryPath)

		val metaGraphDirectoryPath = buildMetaGraphDirectoryPath(metaGraph.uuid)
		ensureDirectory(metaGraphDirectoryPath)

		Files.copy(Path.of(sourceFilePath), buildMetaGraphWritePath(metaGraph.uuid))
	}

	override fun getHistory(metaGraphUuid: UUID): List<MetaGraphHistory> {
		val metaGraphDirectoryPath = buildMetaGraphDirectoryPath(metaGraphUuid)
		if (!Files.exists(metaGraphDirectoryPath)) {
			return emptyList()
		}

		val histories = Files
			.list(metaGraphDirectoryPath)
			.map {
				MetaGraphHistory(
					it.fileName.toString(),
					LocalDateTime.parse(it.fileName.toString(), FILE_NAME_FORMATTER) as LocalDateTime
				)
			}
			.collect(Collectors.toList())

		return histories.sorted().reversed()
	}

	override fun getMetaGraph(library: Library, metaGraphUuid: UUID, history: MetaGraphHistory): MetaGraph {
		FileInputStream(buildMetaGraphReadPath(metaGraphUuid, history.fileName).toString()).use {
			try {
				val storeReader = StoreXmlReader(ElectricXmlReader(it))
				return storeReader.readStorable() as MetaGraph
			} catch (e: Throwable) {
				LOG.error("Error while loading historized MetaGraph ${metaGraphUuid.id}")
				throw e
			}
		}
	}

	override fun deleteHistory(metaGraphUuid: UUID) {
		val metaGraphDirectoryPath = buildMetaGraphDirectoryPath(metaGraphUuid)
		if (Files.exists(metaGraphDirectoryPath)) {
			FileUtils.deleteDirectory(metaGraphDirectoryPath.toFile())
		}
	}

	/** ---- [FileMetaGraphHistoryServiceImpl] */

	private fun buildMetaGraphDirectoryPath(metaGraphUuid: UUID): Path =
		FileSystems.getDefault().getPath(AppModuleJvm.workspaceHolder.userDataDirectoryPath, directoryName, metaGraphUuid.id)

	private fun buildMetaGraphWritePath(metaGraphUuid: UUID): Path {
		val fileName = LocalDateTime.now().format(FILE_NAME_FORMATTER)
		return FileSystems.getDefault().getPath(AppModuleJvm.workspaceHolder.userDataDirectoryPath, directoryName, metaGraphUuid.id, fileName)
	}

	private fun buildMetaGraphReadPath(metaGraphUuid: UUID, fileName: String): Path =
		FileSystems.getDefault().getPath(AppModuleJvm.workspaceHolder.userDataDirectoryPath, directoryName, metaGraphUuid.id, fileName)

	private fun ensureDirectory(path: Path) {
		if (!Files.exists(path)) {
			Files.createDirectory(path)
		}
	}
}

class UnimplementedFileMetaGraphHistoryService : FileMetaGraphHistoryService {

	override fun historize(library: Library, metaGraph: MetaGraph, sourceFilePath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getHistory(metaGraphUuid: UUID): List<MetaGraphHistory> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getMetaGraph(library: Library, metaGraphUuid: UUID, history: MetaGraphHistory): MetaGraph {
		throw UnsupportedOperationException("not implemented")
	}

	override fun deleteHistory(metaGraphUuid: UUID) {
		throw UnsupportedOperationException("not implemented")
	}
}