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

/** Provides methods for managing the set of a user's [Project]s, including open and closing [Project]s. */
class ProjectManagementService(
	private val projectFactory: (TranslatableText) -> Project = ProjectModule.projectFactory,
	libraryService: LibraryService = ProjectModule.projectLibraryService.invoke(),
	private val newMetaGraphNameTranslationKey: String = "project.dialog.metaGraph.name",
	libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	projectDictionaryService: LibraryDictionaryService = ProjectModule.projectDictionaryService,
	systemDictionaryService: LibraryDictionaryService = LibraryModule.systemLibraryDictionaryService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryManagementService(libraryHolder, libraryService, projectDictionaryService, systemDictionaryService,  eventBus) {

	companion object {
		private val LOG by logger(ProjectManagementService::class)
	}

	init {
		eventBus.register(CurrentLibraryEvent::class) {
			if (it.library == null) {
				close()
			}
		}
	}

	/** Determines whether the directory for storing the project [LibraryDictionary] already exists.*/
	val directoryExists: Boolean get() = userDictionaryService.directoryExists

	/** ---- [AbstractLibraryManagementService] */

	override fun existsName(name: TranslatableText, except: UUID?): Boolean =
		userDictionaryService.existsName(name, except)

	/** ---- [ProjectManagementService] */

	fun contains(projectUUID: UUID): Boolean =
		userDictionaryService.contains(projectUUID)

	fun getProjectDirectoryEntries(): ImmutableList<LibraryDictionaryEntry> =
		userDictionaryService.getEntries()

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
	fun create(properties: LibraryProperties): Project {
		if (existsName(properties.name)) {
			throw IllegalArgumentException("project name '${properties.name.getTranslation()}' already exists")
		}
		LOG.trace("creating new project '${properties.name.getTranslation()}'")

		val metaGraph = MetaGraph()
		metaGraph.graph.model!!.name = Name(Translations.getString(newMetaGraphNameTranslationKey))

		val project = projectFactory.invoke(properties.name)
		project.description = Description(properties.description)
		project.visibility = properties.visibility
		properties.importUuid?.let {
			project.addImport(it)
		}
		project.defaultElementUUID = metaGraph.uuid

		libraryService.storeLibrary(project)
		userDictionaryService.add(project)

		libraryService.addContainerLibraryElement(project, metaGraph, project)

		return project
	}

	/** Creates and stores a new [Project], which can be used when the user starts the application the very first time.*/
	fun createHelloProject(library: UUID?): Project =
		create(LibraryProperties(
			TranslatableText(Translations.getString("project.hello.name")),
			importUuid = library
		))

	/**
	 * Updates the currently open [Project] with the specified properties and stores it in persistent store.
	 * @throws IllegalArgumentException if [properties] are not consistent, e.g. if a [Project]
	 * with the specified name already exists.
	 * @throws IllegalStateException if no [Project] is currently open
	 * Posts [LibraryPropertiesEvent] on this [ProjectManagementService]'s [EventBus].
	 */
	fun update(properties: LibraryProperties) {
		if (libraryHolder.l == null) {
			throw IllegalStateException("cannot update properties, no project open")
		}
		val project = libraryHolder.library as Project
		LOG.userTrail("Updating project '${project.uuid}'")

		if (project.name.translation != properties.name) {
			if (existsName(properties.name, except = project.uuid)) {
				throw IllegalArgumentException("project name '${properties.name.getTranslation()}' already exists")
			}
			libraryService.renameLibrary(project, properties.name)
			userDictionaryService.rename(project, properties.name)
		}

		if (properties.author != null && properties.author != project.author) {
			LOG.userTrail("Changing author of Project ${project.uuid}")
		}

		project.properties = properties
		libraryService.storeLibrary(project)
		userDictionaryService.update(project, properties)
		eventBus.post(LibraryPropertiesEvent(project, properties))
	}

	override fun open(libraryId: LibraryIdentification): Project = load(libraryId).also { open(it) }

	/** Opens the specified [Project] and its default [ContainerLibraryElement].*/
	fun open(project: Project) {
		if (libraryHolder.l == null) {
			openImpl(project, project.defaultElementUUID)
		} else {
			eventBus.postVetoable(
				event = OpenLibraryRequest(project),
				undoEvent = OpenLibraryRequest(libraryHolder.library),
				thenHandler = {
					openImpl(project, project.defaultElementUUID)
				}
			)
		}
	}

	/** Opens the specified [Project] and [ContainerLibraryElement].*/
	fun open(libraryId: LibraryIdentification, containerLibraryElement: UUID) {
		val project = load(libraryId)
		if (libraryHolder.l == null) {
			openImpl(project, containerLibraryElement)
		} else {
			eventBus.postVetoable(
				event = OpenLibraryRequest(project),
				undoEvent = OpenLibraryRequest(libraryHolder.library),
				thenHandler = {
					openImpl(project, containerLibraryElement)
				}
			)
		}
	}

	private fun openImpl(project: Project, elementUUID: UUID?) {
		LOG.trace("open project ${project.uuid} with default element $elementUUID")
		libraryHolder.l = project
		if (elementUUID != null) {
			val element = project.getContainerLibraryElement(elementUUID)
			if (element != null) {
				eventBus.post(OpenContainerLibraryElementRequest(element))
			}
		}
	}

	/** Deletes the [Project] with the specified name.*/
	fun delete(libraryId: LibraryIdentification) {
		if (libraryHolder.l?.uuid == libraryId.uuid) {
			closeImpl { deleteImpl(libraryId) }
		} else {
			deleteImpl(libraryId)
		}
	}
}