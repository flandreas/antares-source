package ch.scorpion.jabbah.app.module

import ch.scorpion.jabbah.app.ApplicationVersion
import ch.scorpion.jabbah.app.ApplicationVersionService
import ch.scorpion.jabbah.app.ApplicationVersionServiceImpl
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.module.EditModuleJvm

object AppModuleJvm : AbstractModule() {

	var applicationVersionService: ApplicationVersionService = ApplicationVersionServiceImpl()

	override fun initialize() {
		EditModuleJvm.require()
		AppModule.require()

		fillProperties(BaseModule.properties)
	}

	private fun fillProperties(properties: Properties) {
		properties.set(ApplicationVersionServiceImpl.PROP_IGNORED_VERSION, ApplicationVersion.DUMMY_VERSION)
	}
}