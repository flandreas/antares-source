package io.antarescircuit.jabbah.graph.repository

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.project.ProjectModule

object RepositoryModule : AbstractModule() {

	val repositoryService: RepositoryService = RepositoryServiceImpl()

	override fun initialize() {
		ProjectModule.require()
		LibraryModule.require()
	}

	override fun resetDependencies() {
		ProjectModule.reset()
		LibraryModule.reset()
	}
}