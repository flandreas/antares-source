package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.LineCap
import ch.scorpion.jabbah.draw.graphics.LineJoin
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.select] module.
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

    private fun createRubberBand(): RubberBand = RectangularRubberBand()

    private fun createRubberBandHandler(): RubberBandHandler = RubberBandHandler(createRubberBand())

    private fun fillProperties(properties: Properties) {
        properties.set(RubberBand.PROP_STROKE_PAINT, Color.BLACK)
        properties.set(RubberBand.PROP_FILL_PAINT, Color(255, 200, 0, 32))
        properties.set(RubberBand.PROP_STROKE, Stroke(0.1f, LineCap.SQUARE, LineJoin.MITER, 10.0f, floatArrayOf(5f, 5f), 0f))
        properties.set(RubberBandHandler.PROP_SELECT_STRATEGY, RubberBandHandler.SelectionTimeStrategy.SELECT_ON_DRAG)
        properties.set(RubberBandHandler.PROP_SELECT_TARGET_STRATEGY, RubberBandHandler.SelectionTargetStrategy.CONTAINS.customName)
	    properties.set(RubberBandHandler.PROP_SELECT_DELAY_MS, 200)

        properties.set(Handle.PROP_SIZE_HALF, 4)
        properties.set(Handle.PROP_BORDER_COLOR, Color.BLACK)
        properties.set(Handle.PROP_FILL_COLOR, Color.WHITE)
        properties.set(Handle.PROP_STROKE, Stroke(1.0f))
    }
}