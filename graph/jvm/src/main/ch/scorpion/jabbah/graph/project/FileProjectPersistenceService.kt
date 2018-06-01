package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.MetaGraph
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

/**
 * A [ProjectPersistenceService] that uses the local file system to store projects.
 *
 * @property directoryPath the absolute path of the file system directory where the projects are located
 */
class FileProjectPersistenceService(
	private val directoryPath: String
) : ProjectPersistenceService {

	companion object {
		private val LOG by logger(FileProjectPersistenceService::class)
	}

	/** ---- [ProjectPersistenceService] interface */

	override fun exists(projectName: String): Boolean {
		val path = FileSystems.getDefault().getPath(directoryPath, projectName)
		return Files.exists(path)
	}

	override fun getProjectNames(): ImmutableList<String> {
		return ImmutableList(Files.list(Paths.get(directoryPath))
			.filter({ Files.isDirectory(it) })
			.map { it.fileName.toString() }
			.collect(Collectors.toList()))
	}

	override fun open(projectName: String): MetaGraph {
		throw UnsupportedOperationException("not implemented")
	}
}