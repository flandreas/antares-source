package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionary
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService

/**
 * Posted on [EventBus] when a [Project] is to be opened and is to replace the currently open [Project], if any.
 * Subscribers for this event can raise their veto, such as the application class that keeps track of
 * changes of the current [Project]'s open [MetaGraph].
 */
data class OpenProjectRequest (val project: Project?)

/**
 * Posted on [EventBus] when the currently open [Project] is to be closed.
 * Subscribers for this event can raise their veto, such as the application class that keeps track of
 * changes of the current [Project]'s open [MetaGraph].
 */
data class CloseProjectRequest (val project: Project)

/** Provides methods for managing the set of a user's [Project]s, including open and closing [Project]s. */
class ProjectManagementService(
	private val projectFactory: (TranslatableText) -> Project = ProjectModule.projectFactory,
	libraryService: LibraryService = ProjectModule.projectLibraryService.invoke(),
	private val libraryManagementService: LibraryManagementService = LibraryModule.libraryManagementService,
	private val newMetaGraphNameTranslationKey: String = "project.dialog.metaGraph.name",
	private val projectHolder: ProjectHolder = ProjectModule.projectHolder,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	projectDictionaryService: LibraryDictionaryService = ProjectModule.projectDictionaryService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryManagementService(libraryService, projectDictionaryService, eventBus) {

	companion object {
		private val LOG by logger(ProjectManagementService::class)
	}

	init {
		eventBus.register(CurrentLibraryEvent::class) { close() }
	}

	/** Returns the currently open [Project], if any.*/
	val currentProject: Project? get() = projectHolder.project

	/** Determines whether the directory for storing the project [LibraryDictionary] already exists.*/
	val directoryExists: Boolean get() = dictionaryService.directoryExists


	/** ---- [AbstractLibraryManagementService] */

	override fun hasStaleImportReferences(library: Library): Boolean =
		!libraryManagementService.contains((library as Project).importedLibrary!!)

	override fun existsName(projectName: TranslatableText, except: UUID?): Boolean =
		dictionaryService.existsName(projectName, except)

	/** ---- [ProjectManagementService] */

	fun contains(projectUUID: UUID): Boolean =
		dictionaryService.contains(projectUUID)

	fun getProjectDirectoryEntries(): ImmutableList<LibraryDictionaryEntry> =
		dictionaryService.getEntries()

	/**
	 * Loads the [Project] with the specified [LibraryIdentification].
	 * @throws IllegalArgumentException if a [Project] with [libraryId] doesn't exist
	 */
	fun load(libraryId: LibraryIdentification): Project = libraryService.loadLibrary(libraryId, isSystem = false) as Project

	/**
	 * Creates a new [Project] with the given name and stores it in persistent store.
	 * @return the created [Project]
	 * @throws IllegalArgumentException if [properties] are not consistent, e.g. if a [Project]
	 * with the specified name already exists
	 */
	fun create(properties: LibraryProperties, library: UUID = libraryHolder.library.uuid): Project {
		if (existsName(properties.name)) {
			throw IllegalArgumentException("project name '${properties.name.getTranslation()}' already exists")
		}
		LOG.trace("creating new project '${properties.name.getTranslation()}'")

		val metaGraph = MetaGraph()
		metaGraph.graph.model!!.name = Name(Translations.getString(newMetaGraphNameTranslationKey))

		val project = projectFactory.invoke(properties.name)
		project.description = Description(properties.description)
		project.importedLibrary = library
		project.defaultElementUUID = metaGraph.uuid

		libraryService.storeLibrary(project)
		dictionaryService.add(project)

		libraryService.addContainerLibraryElement(project, metaGraph, project.directory)

		return project
	}

	/** Creates and stores a new [Project], which can be used when the user starts the application the very first time.*/
	fun createHelloProject(library: UUID): Project {
		return create(LibraryProperties(TranslatableText(Translations.getString("project.hello.name"))), library)
	}

	/**
	 * Updates the currently open [Project] with the specified properties and stores it in persistent store.
	 * @throws IllegalArgumentException if [properties] are not consistent, e.g. if a [Project]
	 * with the specified name already exists.
	 * @throws IllegalStateException if no [Project] is currently open
	 * Posts [LibraryPropertiesEvent] on this [ProjectManagementService]'s [EventBus].
	 */
	fun update(properties: LibraryProperties) {
		if (projectHolder.project == null) {
			throw IllegalStateException("cannot update properties, no project open")
		}
		val project = projectHolder.project!!
		LOG.trace("updating project '${project.name}'")

		if (project.name.translation != properties.name) {
			if (existsName(properties.name, except = project.uuid)) {
				throw IllegalArgumentException("project name '${properties.name.getTranslation()}' already exists")
			}
			libraryService.renameLibrary(project, properties.name)
			dictionaryService.rename(project, properties.name)
		}

		project.properties = properties
		libraryService.storeLibrary(project)
		dictionaryService.update(project, properties)
		eventBus.post(LibraryPropertiesEvent(project, properties))
	}

	/**
	 * Loads and opens the [Project] with the specified [UUID], and opens its default [ContainerLibraryElement].
	 * @throws IllegalArgumentException if a [Project] with [libraryId] doesn't exist
	 */
	fun open(libraryId: LibraryIdentification): Project = load(libraryId).also { open(it) }

	/** Opens the specified [Project] and its default [ContainerLibraryElement].*/
	fun open(project: Project) {
		eventBus.postVetoable(
			event = OpenProjectRequest(project),
			undoEvent = OpenProjectRequest(projectHolder.project),
			thenHandler = {
				openImpl(project, project.defaultElementUUID)
			}
		)
	}

	/** Opens the specified [Project] and [ContainerLibraryElement].*/
	fun open(libraryId: LibraryIdentification, containerLibraryElement: UUID) {
		val project = load(libraryId)
		eventBus.postVetoable(
			event = OpenProjectRequest(project),
			undoEvent = OpenProjectRequest(projectHolder.project),
			thenHandler = {
				openImpl(project, containerLibraryElement)
			}
		)
	}

	private fun openImpl(project: Project, elementUUID: UUID?) {
		LOG.trace("open project ${project.uuid} with default element $elementUUID")
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
			project.importedLibrary!!.also {
				val id = if (libraryManagementService.isSystemLibrary(it)) {
					LibraryIdentification(it, null)
				} else {
					LibraryIdentification(it, project.author)
				}
				libraryManagementService.open(id)
			}
		}
	}

	/** Deletes the [Project] with the specified name.*/
	fun delete(libraryId: LibraryIdentification) {
		if (projectHolder.project?.uuid == libraryId.uuid) {
			closeImpl { deleteImpl(libraryId) }
		} else {
			deleteImpl(libraryId)
		}
	}

	/** Closes the currently open [Project].*/
	fun close() {
		closeImpl { }
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