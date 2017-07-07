package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.*

/**
 * Module definitions for the [ch.scorpion.jabbah.draw.style] package.
 */
object DrawStyleModule : AbstractModule() {

    var styleProvider: StyleRepository = StyleRepository.INSTANCE

    /** ---- [AbstractModule] */

    override fun initialize() {
        DrawGraphicsModule.require()

        fillProperties(BaseModule.properties)
        configureStyleRepository(StyleRepository.INSTANCE)
    }

    /** ---- [DrawStyleModule] */

    private fun fillProperties(properties: Properties) {
        properties.predefine(Style.PROP_FOREGROUND_COLOR, Color.BLACK)
        properties.predefine(Style.PROP_BACKGROUND_COLOR, Color.WHITE)
        properties.predefine(Style.PROP_TEXT_COLOR, Color.BLACK)
        properties.predefine(Style.PROP_STROKE, Stroke())
        properties.predefine(Style.PROP_FONT, FontImpl())
    }

    private fun configureStyleRepository(repository: StyleRepository) {
        repository.registerStyle(StyleType.BACKGROUND, BasicStyle())
        repository.registerStyle(StyleType.FIGURE, BasicStyle())
    }
}