package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import javax.swing.SwingUtilities

/**
 * A [ProjectService] that uses the local file system to store projects.
 *
 * @property directoryPath the absolute path of the file system directory where the projects are located
 */
class FileProjectService(
	private val directoryPath: String,
	private val projectFactory: (String) -> Library = ProjectModule.projectFactory,
	private val libraryService: LibraryService = ProjectModule.projectLibraryService.invoke(),
	private val newMetaGraphNameTranslationKey: String = "project.dialog.metaGraph.name",
	private val projectHolder: ProjectHolder = ProjectModule.projectHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : ProjectService {

	companion object {
		private val LOG by logger(FileProjectService::class)
	}

	/** ---- [ProjectService] interface */

	override fun exists(projectName: String): Boolean {
		val path = FileSystems.getDefault().getPath(directoryPath, projectName)
		return Files.exists(path)
	}

	override fun getProjectNames(): ImmutableList<String> {
		return ImmutableList(Files.list(Paths.get(directoryPath))
			.filter({ Files.isDirectory(it) })
			.map { it.fileName.toString() }
			.sorted()
			.collect(Collectors.toList()))
	}

	override fun load(projectName: String): Project {
		if (!exists(projectName)) {
			throw IllegalArgumentException("project name '$projectName' doesn't exists")
		}
		return libraryService.loadLibrary(projectFactory.invoke(projectName))
	}

	override fun create(projectName: String): Project {
		if (exists(projectName)) {
			throw IllegalArgumentException("project name '$projectName' already exists")
		}
		LOG.debug("FileProjectService: creating new project '$projectName'")
		val project = projectFactory.invoke(projectName)
		libraryService.storeLibrary(project)

		val metaGraph = MetaGraph()
		metaGraph.graph!!.model!!.name = Translations.getString(newMetaGraphNameTranslationKey)
		libraryService.addContainerLibraryElement(project, metaGraph, project)

		return project
	}

	override fun open(projectName: String): Project {
		val project = load(projectName)
		open(project)
		return project
	}

	override fun open(project: Project) {
		eventBus.postVetoable(
			event = OpenProjectRequest(project),
			undoEvent = OpenProjectRequest(projectHolder.project),
			thenHandler = {
				projectHolder.p = project
				val defaultElement = project.getDefaultElement()
				if (defaultElement != null) {
					eventBus.post(OpenContainerLibraryElementRequest(defaultElement))
				}
			}
		)
	}
}