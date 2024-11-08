package ch.scorpion.jabbah.graph.repository

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule

object RepositoryModule : AbstractModule() {

	val repositoryService: RepositoryService = RepositoryServiceImpl()

	override fun initialize() {
		ProjectModule.require()
		LibraryModule.require()
	}
}