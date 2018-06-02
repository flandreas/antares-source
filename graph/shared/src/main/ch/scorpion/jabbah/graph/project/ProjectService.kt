package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.graph.library.Library

typealias Project = Library

interface ProjectService {

	/** Returns the names of all stored projects.*/
	fun getProjectNames(): ImmutableList<String>

	/** Determines whether [projectName] already exists as the name of a stored project.*/
	fun exists(projectName: String): Boolean

	/**
	 * Opens and loads the [Project] with the specified name.
	 * @throws IllegalArgumentException if a project with name [projectName] doesn't exist
	 */
	fun open(projectName: String): Project

	/**
	 * Creates a new [Project] with the given name and stores it in persistent store.
	 * @return the created [Project]
	 * @throws IllegalArgumentException if a project with name [projectName] already exists
	 */
	fun create(projectName: String): Project
}

/** Null pattern */
class UnimplementedProjectService : ProjectService {

	override fun getProjectNames(): ImmutableList<String> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exists(projectName: String): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	override fun open(projectName: String): Project {
		throw UnsupportedOperationException("not implemented")
	}

	override fun create(projectName: String): Project {
		throw UnsupportedOperationException("not implemented")
	}
}