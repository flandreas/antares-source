package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View

/**
 * Module definitions for the [ch.scorpion.jabbah.draw.view] package.
 */
object DrawViewModule : AbstractModule() {

    var viewManager: ViewManager = ViewManagerImpl(BaseModule.eventBus)

    override fun initialize() {
	    BaseModule.require()
	    fillProperties(BaseModule.properties)
        TooltipManager.eventBus = BaseModule.eventBus
    }

	private fun fillProperties(properties: Properties) {
		properties.set(View.PROP_MIN_ZOOM_FACTOR, 0.05f)
		properties.set(View.PROP_MAX_ZOOM_FACTOR, 20f)
		properties.set(View.PROP_DEFAULT_ZOOM_FACTOR, 1.0f)
		properties.set(ZoomPanController.PROP_WHEEL_ZOOM_STEP, 1.1f)
		properties.set(ZoomPanController.PROP_WHEEL_PAN_STEP, 5)
		properties.set(TooltipManager.PROP_DELAY, 1500)
	}
}