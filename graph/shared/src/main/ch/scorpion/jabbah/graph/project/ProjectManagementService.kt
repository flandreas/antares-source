package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
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

	/** Determines whether [projectName] already exists as the name of a stored project.*/
	fun exists(projectName: String): Boolean

	/**
	 * Loads the [Project] with the specified name.
	 * @throws IllegalArgumentException if a project with name [projectName] doesn't exist
	 */
	fun load(projectName: String): Project

	/**
	 * Creates a new [Project] with the given name and stores it in persistent store.
	 * @return the created [Project]
	 * @throws IllegalArgumentException if [proprties] are not consistent, e.g. if a [Project]
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
	 * @throws IllegalArgumentException if a project with name [projectName] doesn't exist
	 */
	fun open(projectName: String): Project

	/** Opens the specified [Project] and its default [ContainerLibraryElement].*/
	fun open(project: Project)

	/** Opens the specified [Project] and [ContainerLibraryElement].*/
	fun open(projectName: String, containerLibraryElement: UUID)

	/** Deletes the [Project] with the specified name.*/
	fun delete(projectName: String)

	/** Closes the currently open [Project].*/
	fun close()
}

/** Null pattern */
class UnimplementedProjectManagementService : ProjectManagementService {

	override val currentProject: Project? get() = throw UnsupportedOperationException("not implemented")

	override fun getProjectNames(): ImmutableList<String> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exists(projectName: String): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	override fun load(projectName: String): Project {
		throw UnsupportedOperationException("not implemented")
	}

	override fun create(properties: LibraryProperties): Project {
		throw UnsupportedOperationException("not implemented")
	}

	override fun update(properties: LibraryProperties) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun open(projectName: String): Project {
		throw UnsupportedOperationException("not implemented")
	}

	override fun open(project: Project) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun open(projectName: String, containerLibraryElement: UUID) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun delete(projectName: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun close() {
		throw UnsupportedOperationException("not implemented")
	}
}