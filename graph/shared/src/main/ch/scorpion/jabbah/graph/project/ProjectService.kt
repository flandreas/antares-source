package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.graph.library.Library

typealias Project = Library

/**
 * Posted on [EventBus] when a [Project] is to be opened and is to replace the currently open [Project], if any.
 * Subscribers for this event can raise their veto, such as the application class that keeps track of
 * changes of the current [Project]'s open [MetaGraph].
 */
data class OpenProjectRequest (val project: Project?)

interface ProjectService {

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
	 * @throws IllegalArgumentException if a project with name [projectName] already exists
	 */
	fun create(projectName: String): Project

	/**
	 * Loads and opens the [Project] with the specified name.
	 * @throws IllegalArgumentException if a project with name [projectName] doesn't exist
	 */
	fun open(projectName: String): Project

	/** Opens the specified [Project] in the current [Application].*/
	fun open(project: Project)
}

/** Null pattern */
class UnimplementedProjectService : ProjectService {

	override fun getProjectNames(): ImmutableList<String> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exists(projectName: String): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	override fun load(projectName: String): Project {
		throw UnsupportedOperationException("not implemented")
	}

	override fun create(projectName: String): Project {
		throw UnsupportedOperationException("not implemented")
	}

	override fun open(projectName: String): Project {
		throw UnsupportedOperationException("not implemented")
	}

	override fun open(project: Project) {
		throw UnsupportedOperationException("not implemented")
	}
}