package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import org.apache.commons.io.FilenameUtils

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
	private val libraryDictionaryService: LibraryDictionaryService = LibraryModule.libraryDictionaryService,
	private val projectDictionaryService: LibraryDictionaryService = ProjectModule.projectDictionaryService,
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

	override fun exists(projectName: String): Boolean =
		projectDictionaryService.existsName(projectName)

	override fun getProjectNames(): ImmutableList<String> =
		projectDictionaryService.getNames()

	override fun getProjectDirectoryEntries(): ImmutableList<LibraryDictionaryEntry> =
		projectDictionaryService.getEntries()

	override fun load(uuid: UUID): Project = libraryService.loadLibrary(uuid) as Project

	override fun create(properties: LibraryProperties): Project {
		if (exists(properties.name)) {
			throw IllegalArgumentException("project name '${properties.name}' already exists")
		}
		LOG.debug("creating new project '${properties.name}'")
		val project = projectFactory.invoke(properties.name)
		project.description = properties.description
		project.importedLibrary = libraryHolder.library.uuid
		libraryService.storeLibrary(project)
		projectDictionaryService.add(project)

		val metaGraph = MetaGraph()
		metaGraph.graph.model!!.name.value = Translations.getString(newMetaGraphNameTranslationKey)
		libraryService.addContainerLibraryElement(project, metaGraph, project)

		return project
	}

	override fun update(properties: LibraryProperties) {
		if (projectHolder.project == null) {
			throw IllegalStateException("cannot update properties, no project open")
		}
		val project = projectHolder.project!!
		LOG.debug("updating project '${project.name}'")

		if (project.name != properties.name) {
			if (exists(properties.name)) {
				throw IllegalArgumentException("project name '${properties.name}' already exists")
			}
			libraryService.renameLibrary(project, properties.name)
			projectDictionaryService.rename(project, properties.name)
		}

		project.properties = properties
		libraryService.storeLibrary(project)
		projectDictionaryService.update(project, properties)
		eventBus.post(LibraryPropertiesEvent(project, properties))
	}

	override fun open(uuid: UUID): Project = load(uuid).also { open(it) }

	override fun open(project: Project) {
		eventBus.postVetoable(
			event = OpenProjectRequest(project),
			undoEvent = OpenProjectRequest(projectHolder.project),
			thenHandler = {
				openImpl(project, project.defaultElementUUID)
			}
		)
	}

	override fun open(uuid: UUID, containerLibraryElement: UUID) {
		val project = load(uuid)
		eventBus.postVetoable(
			event = OpenProjectRequest(project),
			undoEvent = OpenProjectRequest(projectHolder.project),
			thenHandler = {
				openImpl(project, containerLibraryElement)
			}
		)
	}

	private fun openImpl(project: Project, elementUUID: UUID?) {
		LOG.debug("open project ${project.uuid}")
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

	override fun delete(uuid: UUID) {
		if (projectHolder.project?.uuid == uuid) {
			closeImpl { deleteImpl(uuid) }
		} else {
			deleteImpl(uuid)
		}
	}

	private fun deleteImpl(uuid: UUID) {
		libraryService.deleteLibrary(uuid)
		projectDictionaryService.remove(uuid)
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

	override fun export(uuid: UUID, outputPath: String) {
		libraryService.exportLibrary(uuid, outputPath)
	}

	override fun import(inputPath: String): ProjectImportResult {
		val uuid = UUID(FilenameUtils.getBaseName(inputPath))

		val library = libraryService.importLibrary(uuid, inputPath) ?: return ProjectImportResult.Invalid

		if (exists(library.name)) {
			LOG.debug("Name of imported project already exists")
			libraryService.purgeLibrary(uuid)
			return ProjectImportResult.NameAlreadyExists
		}

		if (!libraryDictionaryService.contains((library as Project).uuid)) {
			LOG.debug("Library for imported project doesn't exist")
			libraryService.purgeLibrary(uuid)
			return ProjectImportResult.StaleLibraryReference
		}

		return ProjectImportResult.Success
	}
}