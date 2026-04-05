package io.antarescircuit.jabbah.draw.style

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.graphics.*

/**
 * Module definitions for the [io.antarescircuit.jabbah.draw.style] package.
 */
object DrawStyleModule : AbstractModule() {

    var styleProvider: StyleRepository = StyleRepository.INSTANCE

    /** ---- [AbstractModule] */

    override fun initialize() {
        DrawGraphicsModule.require()

        fillProperties(BaseModule.properties)
        configureStyleRepository(StyleRepository.INSTANCE)
    }

    override fun resetDependencies() {
        DrawGraphicsModule.reset()
    }

    /** ---- [DrawStyleModule] */

    private fun fillProperties(properties: Properties) {
        properties.set(Style.PROP_FOREGROUND_COLOR, Color.BLACK)
        properties.set(Style.PROP_BACKGROUND_COLOR, Color.WHITE)
        properties.set(Style.PROP_TEXT_COLOR, Color.BLACK)
        properties.set(Style.PROP_STROKE, Stroke())
        properties.set(Style.PROP_FONT, FontImpl())
    }

    private fun configureStyleRepository(repository: StyleRepository) {
        repository.registerStyle(StyleType.BACKGROUND, BasicStyle())
        repository.registerStyle(StyleType.FIGURE, BasicStyle())
	    repository.registerStyle(StyleType.ANNOTATION, BasicStyle())
        repository.registerStyle(StyleType.TOOLTIP, BasicStyle())
    }
}