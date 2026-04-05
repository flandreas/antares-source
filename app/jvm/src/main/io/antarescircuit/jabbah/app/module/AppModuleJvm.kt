package io.antarescircuit.jabbah.app.module

import io.antarescircuit.jabbah.app.*
import io.antarescircuit.jabbah.app.dump.SystemDumpService
import io.antarescircuit.jabbah.app.rating.RailwayRatingService
import io.antarescircuit.jabbah.app.rating.RatingService
import io.antarescircuit.jabbah.app.workspace.WorkspaceService
import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.module.EditModuleJvm

object AppModuleJvm : AbstractModule() {

	var remotePropertiesUrl: String? = null
	val remoteControlService = RemoteControlService()

	var applicationUsageService: ApplicationUsageService = RailwayAppUsageServiceImpl()
	val ratingService: RatingService = RailwayRatingService()
	var systemDumpService: SystemDumpService = SystemDumpService()

	val workspaceHolder = WorkspaceHolder()
	val workspaceService = WorkspaceService()

	override fun initialize() {
		EditModuleJvm.require()
		AppModule.require()

		fillProperties(BaseModule.properties)
	}

	override fun resetDependencies() {
		EditModuleJvm.reset()
		AppModule.reset()
	}

	private fun fillProperties(properties: Properties) {
		properties.set(RemoteControlService.PROP_IGNORED_VERSION, ApplicationVersion.DUMMY_VERSION_ID)
	}
}