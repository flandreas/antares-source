package ch.scorpion.antares.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.graph.library.AkrabRestLibraryPersistenceService
import ch.scorpion.jabbah.graph.project.ProjectModule

/**
 * Overwrites [AntaresModuleJs] to work with Akrab REST endpoints.
 */
object AntaresAkrabModuleJs : AbstractModule() {

	override fun initialize() {
		AntaresModuleJs.require()

		println("Configuring ProjectLibraryPersistenceService")

		ProjectModule.projectLibraryPersistenceService = AkrabRestLibraryPersistenceService(
			baseUrl = "http://localhost:9999/api/projects",
			dictionaryName = "circuits"
		)
	}
}