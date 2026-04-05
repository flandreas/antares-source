package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.LineCap
import io.antarescircuit.jabbah.draw.graphics.LineJoin
import io.antarescircuit.jabbah.draw.graphics.Stroke

/**
 * Module definitions for the [io.antarescircuit.jabbah.edit.select] module.
 */
object EditSelectModule : AbstractModule() {

    var selectionModelFactory: SelectionModelFactory = SelectionModelFactoryImpl()

    var selectionModelProvider: SelectionModelProvider = SimpleSelectionModelProvider(selectionModelFactory)

	var selectionManagerFactory: SelectionManagerFactory = { content -> SelectionManagerImpl(content) }

	var selectionToolFactory: SelectionToolFactory =
		{ editor -> SelectionToolImpl(editor, createRubberBandHandler(), BaseModule.eventBus)}

    override fun initialize() {
        fillProperties(BaseModule.properties)
    }

    override fun resetDependencies() {}

    private fun createRubberBand(): RubberBand = RectangularRubberBand()

    private fun createRubberBandHandler(): RubberBandHandler = RubberBandHandler(createRubberBand())

    private fun fillProperties(properties: Properties) {
        properties.set(RubberBand.PROP_STROKE_PAINT, Color.BLACK)
        properties.set(RubberBand.PROP_FILL_PAINT, Color(255, 200, 0, 32))
        properties.set(RubberBand.PROP_STROKE, Stroke(0.1f, LineCap.SQUARE, LineJoin.MITER, 10.0f, floatArrayOf(5f, 5f), 0f))
        properties.set(RubberBandHandler.PROP_SELECT_TARGET_STRATEGY, RubberBandHandler.SelectionTargetStrategy.CONTAINS.customName)
	    properties.set(RubberBandHandler.PROP_SELECT_DELAY_MS, 200)

        properties.set(Handle.PROP_SIZE_HALF, 4)
        properties.set(Handle.PROP_BORDER_COLOR, Color.BLACK)
        properties.set(Handle.PROP_FILL_COLOR, Color.WHITE)
        properties.set(Handle.PROP_STROKE, Stroke(1.0f))
    }
}