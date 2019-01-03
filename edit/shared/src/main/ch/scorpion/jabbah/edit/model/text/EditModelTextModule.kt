package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.Grid
import ch.scorpion.jabbah.edit.GridPainter
import ch.scorpion.jabbah.edit.GridPainterRegistry
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangularHandleSelectionModel
import ch.scorpion.jabbah.edit.model.rectangle.RectangularReplaceSelectionModel
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.snap.DottedGridPainter
import ch.scorpion.jabbah.edit.snap.LineGridPainter

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.model.text] package.
 */
object EditModelTextModule : AbstractModule() {

    override fun initialize() {
	    GridPainterRegistry.register(DottedGridPainter.NAME) { s -> DottedGridPainter(s) }
	    GridPainterRegistry.register(LineGridPainter.NAME) { s -> LineGridPainter(s) }

        EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.ABOVE,
            TextComponent::class.simpleName!!)
        { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }

	    EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.ABOVE,
            SimpleTextComponent::class.simpleName!!)
	    { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }

	    EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.REPLACE,
            SimpleTextComponent::class.simpleName!!)
	    { RectangularReplaceSelectionModel(it as AbstractRectangularComponent) }

	    fillProperties(DrawModule.properties)
    }

	private fun fillProperties(properties: Properties) {
		properties.set(Grid.PROP_GRID_PAINTER, LineGridPainter.NAME)
	}
}