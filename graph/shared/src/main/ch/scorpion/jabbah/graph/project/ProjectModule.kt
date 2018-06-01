package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.AbstractModule

/**
 * Module definitions the the [ch.scorpion.jabbah.graph.project] module.
 */
object ProjectModule : AbstractModule() {

	var projectPersistenceService: ProjectPersistenceService = UnimplementedProjectPersistenceService()

	override fun initialize() {
		// empty
	}

}