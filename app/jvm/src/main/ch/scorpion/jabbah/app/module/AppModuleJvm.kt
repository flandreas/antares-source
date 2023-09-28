package ch.scorpion.jabbah.app.module

import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.app.dump.SystemDumpService
import ch.scorpion.jabbah.app.rating.RailwayRatingService
import ch.scorpion.jabbah.app.rating.RatingService
import ch.scorpion.jabbah.app.workspace.WorkspaceService
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.module.EditModuleJvm

object AppModuleJvm : AbstractModule() {

	var remotePropertiesUrl: String? = null
	val remoteControlService = RemoteControlService()

	var applicationUsageService: ApplicationUsageService = RailwayAppUsageServiceImpl()
	val ratingService: RatingService = RailwayRatingService()
	var systemDumpService: SystemDumpService = SystemDumpService()

	lateinit var workspaceHolder: WorkspaceHolder
	val workspaceService = WorkspaceService()

	override fun initialize() {
		EditModuleJvm.require()
		AppModule.require()

		fillProperties(BaseModule.properties)
	}

	private fun fillProperties(properties: Properties) {
		properties.set(RemoteControlService.PROP_IGNORED_VERSION, ApplicationVersion.DUMMY_VERSION_ID)
	}
}