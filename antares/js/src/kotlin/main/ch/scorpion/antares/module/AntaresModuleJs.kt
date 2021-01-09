package ch.scorpion.antares.module

import ch.scorpion.antares.view.AntaresLibraryFactory
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.module.EditModuleJs
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.RestLibraryPersistenceService
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJs

/**
 * Module definition of the [ch.scorpion.antares] module for the JS platform.
 */
object AntaresModuleJs : AbstractModule() {

	override fun initialize() {
		EditModuleJs.require()
		GraphViewModuleJs.require()
		AntaresViewModule.require()

		LibraryModule.userLibraryPersistenceService = RestLibraryPersistenceService()
		LibraryModule.libraryFactory = AntaresLibraryFactory()
	}
}