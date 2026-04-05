package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.View

/**
 * Module definitions for the [io.antarescircuit.jabbah.draw.view] package.
 */
object DrawViewModule : AbstractModule() {

    var viewManager: ContentViewManager = ContentViewManagerImpl(BaseModule.eventBus)

    override fun initialize() {
	    BaseModule.require()
	    fillProperties(BaseModule.properties)
        TooltipManager.eventBus = BaseModule.eventBus
    }

	override fun resetDependencies() {
		BaseModule.reset()
	}

	private fun fillProperties(properties: Properties) {
		properties.set(View.PROP_MIN_ZOOM_FACTOR, 0.05f)
		properties.set(View.PROP_MAX_ZOOM_FACTOR, 20f)
		properties.set(View.PROP_DEFAULT_ZOOM_FACTOR, 1.0f)
		properties.set(ZoomPanController.PROP_WHEEL_ZOOM_STEP, 1.1f)
		properties.set(ZoomPanController.PROP_WHEEL_ZOOM_REQUIRES_META, false)
		properties.set(ZoomPanController.PROP_WHEEL_PAN_STEP, 5)
		properties.set(TooltipHandler.PROP_TOOLTIPS_ENABLED, true)
		properties.set(TooltipManager.PROP_DELAY, 1500)
	}
}