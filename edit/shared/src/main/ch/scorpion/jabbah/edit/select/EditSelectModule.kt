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
 * Module definitions for the [jabbah.edit.select] module.
 */
object EditSelectModule : AbstractModule() {

    var selectionModelFactory: SelectionModelFactory = SelectionModelFactoryImpl()

    var selectionModelProvider: SelectionModelProvider = SimpleSelectionModelProvider(selectionModelFactory)

    var selectionManagerFactory = object : SelectionManagerFactory {
        override fun create(view: DrawingView<out Drawing<Component>>): SelectionManager {
            return SelectionManagerImpl(view)
        }
    }

    var selectionToolFactory = object : SelectionToolFactory {
        override fun create(editor: Editor): SelectionTool {
            return createSelectionTool(editor)
        }
    }

    override fun initialize() {
        fillProperties()
    }

    private fun createRubberBand(): RubberBand {
        return RectangularRubberBand()
    }

    private fun createRubberBandHandler(): RubberBandHandler {
        return RubberBandHandler(createRubberBand())
    }

    private fun createSelectionTool(editor: Editor): SelectionTool {
        return SelectionToolImpl(editor, createRubberBandHandler(), BaseModule.eventBus)
    }

    private fun fillProperties() {
        BaseModule.properties.predefine(RubberBand.PROP_STROKE_PAINT, Color.BLACK)
        BaseModule.properties.predefine(RubberBand.PROP_FILL_PAINT, Color(255, 200, 0, 32))
        BaseModule.properties.predefine(RubberBand.PROP_STROKE, Stroke(0.1f, LineCap.SQUARE, LineJoin.MITER, 10.0f, floatArrayOf(5f, 5f), 0f))
        BaseModule.properties.predefine(RubberBandHandler.PROP_SELECT_STRATEGY, RubberBandHandler.SelectionStrategy.SELECT_ON_DRAG)

        BaseModule.properties.predefine(Handle.PROP_SIZE_HALF, 4)
        BaseModule.properties.predefine(Handle.PROP_BORDER_COLOR, Color.BLACK)
        BaseModule.properties.predefine(Handle.PROP_FILL_COLOR, Color.WHITE)
        BaseModule.properties.predefine(Handle.PROP_STROKE, Stroke(1.0f))
    }
}