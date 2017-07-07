package ch.scorpion.jabbah.edit.snap

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Grid
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * Module definitions for the [jabbah.edit.snap] module.
 */
object EditSnapModule : AbstractModule() {

    override fun initialize() {
        fillProperties(BaseModule.properties)
    }

    private fun fillProperties(properties: Properties) {
        properties.predefine(Grid.PROP_GRID_DEFAULT_DISTANCE, 10)
        properties.predefine(Grid.PROP_GRID_MIN_DISTANCE, 8)
        properties.predefine(Grid.PROP_GRID_DEFAULT_PAINT_FACTOR, 2)

        properties.predefine(ComponentSnapper.PROP_SNAP_HIGHLIGHT_COLOR, Color.ORANGE)
        properties.predefine(ComponentSnapper.PROP_SNAP_HIGHLIGHT_STROKE, Stroke(1.0f))
    }
}