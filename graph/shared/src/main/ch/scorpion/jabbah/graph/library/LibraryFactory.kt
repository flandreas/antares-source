package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.project.Project

/**
 * A factory for creating new [Libraries][Library].
 */
interface LibraryFactory {

	/** Creates a new empty [Library] with the specified properties.*/
	fun createEmptyLibrary(properties: LibraryProperties, importedLibraryId: LibraryIdentification? = null): Library

	/**
	 * Creates a new [Library] that contains the [BaseLibraryElement]s of a particular application.
	 * This can be used as a starting point when creating new user provided [Libraries][Library].
	 */
	fun createBaseLibrary(properties: LibraryProperties): Library

	/**
	 * Fills the specified [library] with preferences from [BaseModule.properties].
	 * This can't be solely implemented in [createEmptyLibrary], because this [LibraryFactory] is also
	 * used by code that creates [Project], which must be initialized with the same preferences.
	 */
	fun fillPreferences(library: Library)
}
