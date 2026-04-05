package io.antarescircuit.jabbah.edit.snap

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.edit.Grid

/**
 * Module definitions for the [io.antarescircuit.jabbah.edit.snap] module.
 */
object EditSnapModule : AbstractModule() {

    override fun initialize() {
        fillProperties(BaseModule.properties)
    }

    override fun resetDependencies() {}

    private fun fillProperties(properties: Properties) {
        properties.set(Grid.PROP_GRID_DEFAULT_DISTANCE, 10)
        properties.set(Grid.PROP_GRID_MIN_DISTANCE, 8)
        properties.set(Grid.PROP_GRID_DEFAULT_PAINT_FACTOR, 2)

        properties.set(ComponentSnapper.PROP_SNAP_HIGHLIGHT_COLOR, Color.BLUE)
        properties.set(ComponentSnapper.PROP_SNAP_HIGHLIGHT_STROKE, Stroke(0.5f))
    }
}