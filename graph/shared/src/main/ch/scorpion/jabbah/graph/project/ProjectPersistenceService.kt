package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.base.exception.IllegalArgumentException

interface ProjectPersistenceService {

	/** Returns the names of all stored projects.*/
	fun getProjectNames(): ImmutableList<String>

	/** Determines whether [projectName] already exists as the name of a stored project.*/
	fun exists(projectName: String): Boolean

	/**
	 * Opens and loads the [MetaGraph] project with the specified name.
	 * @throws IllegalArgumentException if a project with name [name] doesn't exist
	 */
	fun open(projectName: String): MetaGraph
}

/** Null pattern */
class UnimplementedProjectPersistenceService : ProjectPersistenceService {

	override fun getProjectNames(): ImmutableList<String> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exists(projectName: String): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	override fun open(projectName: String): MetaGraph {
		throw UnsupportedOperationException("not implemented")
	}

}