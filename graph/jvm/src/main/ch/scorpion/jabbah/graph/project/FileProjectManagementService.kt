package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import org.apache.commons.io.FileUtils
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

/**
 * A [ProjectManagementService] that uses the local file system to store [Project]s.
 *
 * @property directoryPath the absolute path of the file system directory where the [Project]s are located
 */
class FileProjectManagementService(
	private val directoryPath: String,
	private val projectFactory: (String) -> Project = ProjectModule.projectFactory,
	private val libraryService: LibraryService = ProjectModule.projectLibraryService.invoke(),
	private val libraryManagementService: LibraryManagementService = LibraryModule.libraryManagementService,
	private val newMetaGraphNameTranslationKey: String = "project.dialog.metaGraph.name",
	private val projectHolder: ProjectHolder = ProjectModule.projectHolder,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : ProjectManagementService {

	companion object {
		private val LOG by logger(FileProjectManagementService::class)
	}

	init {
		eventBus.register(CurrentLibraryEvent::class) { close() }
	}

	/** ---- [ProjectManagementService] interface */

	override val currentProject: Project? get() = projectHolder.project

	override fun exists(projectName: String): Boolean {
		val path = FileSystems.getDefault().getPath(directoryPath, projectName)
		return Files.exists(path)
	}

	override fun getProjectNames(): ImmutableList<String> {
		return ImmutableList(Files.list(Paths.get(directoryPath))
			.filter { Files.isDirectory(it) }
			.map { it.fileName.toString() }
			.sorted()
			.collect(Collectors.toList()))
	}

	override fun load(projectName: String): Project {
		if (!exists(projectName)) {
			throw IllegalArgumentException("project name '$projectName' doesn't exists")
		}
		return libraryService.loadLibrary(projectName) as Project
	}

	override fun create(projectName: String): Project {
		if (exists(projectName)) {
			throw IllegalArgumentException("project name '$projectName' already exists")
		}
		LOG.debug("FileProjectManagementService: creating new project '$projectName'")
		val project = projectFactory.invoke(projectName)
		project.importedLibrary = libraryHolder.library.uuid
		libraryService.storeLibrary(project)

		val metaGraph = MetaGraph()
		metaGraph.graph.model!!.name = Translations.getString(newMetaGraphNameTranslationKey)
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
				openImpl(project, project.defaultElementUUID)
			}
		)
	}

	override fun open(projectName: String, containerLibraryElement: UUID) {
		val project = load(projectName)
		eventBus.postVetoable(
			event = OpenProjectRequest(project),
			undoEvent = OpenProjectRequest(projectHolder.project),
			thenHandler = {
				openImpl(project, containerLibraryElement)
			}
		)
	}

	private fun openImpl(project: Project, elementUUID: UUID?) {
		LOG.debug("FileProjectManagementService: open project '${project.name}'")
		openLibraryForProjectIfNecessary(project)
		projectHolder.p = project
		if (elementUUID != null) {
			val element = project.getContainerLibraryElement(elementUUID)
			if (element != null) {
				eventBus.post(OpenContainerLibraryElementRequest(element))
			}
		}
	}

	private fun openLibraryForProjectIfNecessary(project: Project) {
		if (project.importedLibrary != libraryHolder.library.uuid) {
			libraryManagementService.open(project.importedLibrary!!)
		}
	}

	override fun delete(projectName: String) {
		if (projectHolder.project != null && projectHolder.project!!.name == projectName) {
			closeImpl { deleteImpl(projectName) }
		} else {
			deleteImpl(projectName)
		}
	}

	private fun deleteImpl(projectName: String) {
		FileUtils.forceDelete(FileSystems.getDefault().getPath(directoryPath, projectName).toFile())
	}

	override fun close() {
		closeImpl {}
	}

	private fun closeImpl(additionalThenHandler: () -> Unit) {
		val project = projectHolder.project
		if (project != null) {
			eventBus.postVetoable(
				event = CloseProjectRequest(project),
				undoEvent = OpenProjectRequest(project),
				thenHandler = {
					projectHolder.p = null
					additionalThenHandler.invoke()
				}
			)
		}
	}
}