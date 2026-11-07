package io.antarescircuit.jabbah.app.module

import io.antarescircuit.jabbah.app.rating.RailwayRatingService
import io.antarescircuit.jabbah.app.rating.RatingService
import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.edit.module.EditModuleUiJvm

/** Initializes desktop adapters and remote desktop services. */
object AppModuleUiJvm : AbstractModule() {

	val ratingService: RatingService by lazy { RailwayRatingService() }

	override fun initialize() {
		EditModuleUiJvm.require()
		AppModuleJvm.require()
	}

	override fun resetDependencies() {
		EditModuleUiJvm.reset()
		AppModuleJvm.reset()
	}
}
