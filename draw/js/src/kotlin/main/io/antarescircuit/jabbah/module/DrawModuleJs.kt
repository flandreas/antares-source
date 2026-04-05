package io.antarescircuit.jabbah.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.module.BaseModuleJs
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.draw.view.TooltipHandler
import io.antarescircuit.jabbah.draw.view.ZoomPanController

/**
 * Module definitions for the [io.antarescircuit.jabbah.draw] package for the JavaScript target.
 */
object DrawModuleJs : AbstractModule() {

	override fun initialize() {
		BaseModuleJs.require()
		DrawModule.require()

		fillProperties(BaseModule.properties)
	}

	override fun resetDependencies() {
		BaseModuleJs.reset()
		DrawModule.reset()
	}

	private fun fillProperties(properties: Properties) {
		// Disable textual HTML tooltips in JS until HTML text boxed can be rendered
		properties.set(TooltipHandler.PROP_TOOLTIPS_ENABLED, true)

		// Zooming with mouse wheel requires META key in order not to interfere with scrolling on web page
		properties.set(ZoomPanController.PROP_WHEEL_ZOOM_REQUIRES_META, true)
	}
}