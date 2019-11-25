package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.library.LibraryProperties
import ch.scorpion.jabbah.graph.library.LibraryPropertiesEvent

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
interface ProjectManagementService {

	/** Returns the currently open [Project], if any.*/
	val currentProject: Project?

	/** Returns the names of all stored projects.*/
	fun getProjectNames(): ImmutableList<String>

	fun getProjectDirectoryEntries(): ImmutableList<LibraryDictionaryEntry>

	/** Determines whether [projectName] already exists as the name of a stored project.*/
	fun exists(projectName: String): Boolean

	/**
	 * Loads the [Project] with the specified [UUID].
	 * @throws IllegalArgumentException if a project with name [uuid] doesn't exist
	 */
	fun load(uuid: UUID): Project

	/**
	 * Creates a new [Project] with the given name and stores it in persistent store.
	 * @return the created [Project]
	 * @throws IllegalArgumentException if [properties] are not consistent, e.g. if a [Project]
	 * with the specified name already exists
	 */
	fun create(properties: LibraryProperties): Project

	/**
	 * Updates the currently open [Project] with the specified properties and stores it in persistent store.
	 * @throws IllegalArgumentException if [properties] are not consistent, e.g. if a [Project]
	 * with the specified name already exists.
	 * @throws IllegalStateException if no [Project] is currently open
	 * Posts [LibraryPropertiesEvent] on this [ProjectManagementService]'s [EventBus].
	 */
	fun update(properties: LibraryProperties)

	/**
	 * Loads and opens the [Project] with the specified name, and opens its default [ContainerLibraryElement].
	 * @throws IllegalArgumentException if a project with name [uuid] doesn't exist
	 */
	fun open(uuid: UUID): Project

	/** Opens the specified [Project] and its default [ContainerLibraryElement].*/
	fun open(project: Project)

	/** Opens the specified [Project] and [ContainerLibraryElement].*/
	fun open(uuid: UUID, containerLibraryElement: UUID)

	/** Deletes the [Project] with the specified name.*/
	fun delete(uuid: UUID)

	/** Closes the currently open [Project].*/
	fun close()

	fun export(uuid: UUID, outputPath: String)

	fun import(inputPath: String): ProjectImportResult
}

enum class ProjectImportResult {
	Success,
	NameAlreadyExists,
	Invalid,
	StaleLibraryReference
}

/** Null pattern */
class UnimplementedProjectManagementService : ProjectManagementService {

	override val currentProject: Project? get() =
		throw NotImplementedError()

	override fun getProjectNames(): ImmutableList<String> =
		throw NotImplementedError()

	override fun getProjectDirectoryEntries(): ImmutableList<LibraryDictionaryEntry> {
		throw NotImplementedError()
	}

	override fun exists(projectName: String): Boolean =
		throw NotImplementedError()

	override fun load(uuid: UUID): Project =
		throw NotImplementedError()

	override fun create(properties: LibraryProperties): Project =
		throw NotImplementedError()

	override fun update(properties: LibraryProperties): Unit =
		throw NotImplementedError()

	override fun open(uuid: UUID): Project =
		throw NotImplementedError()

	override fun open(project: Project): Unit =
		throw NotImplementedError()

	override fun open(uuid: UUID, containerLibraryElement: UUID): Unit =
		throw NotImplementedError()

	override fun delete(uuid: UUID): Unit =
		throw NotImplementedError()

	override fun close(): Unit =
		throw NotImplementedError()

	override fun export(uuid: UUID, outputPath: String): Unit =
		throw NotImplementedError()

	override fun import(inputPath: String): ProjectImportResult =
		throw NotImplementedError()
}