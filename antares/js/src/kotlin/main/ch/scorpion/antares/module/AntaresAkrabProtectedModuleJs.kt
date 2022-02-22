package ch.scorpion.antares.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.graph.library.AkrabRestLibraryPersistenceServiceJs
import ch.scorpion.jabbah.graph.project.ProjectModule

/**
 * Extends [AntaresModuleJs] to work with Akrab protected REST endpoints.
 */
object AntaresAkrabProtectedModuleJs : AbstractModule() {

	override fun initialize() {
		AntaresModuleJs.require()

		ProjectModule.projectLibraryPersistenceService = AkrabRestLibraryPersistenceServiceJs(
			baseUrl = "http://localhost:9999/api/projects",
			dictionaryName = "circuits"
		)
	}
}