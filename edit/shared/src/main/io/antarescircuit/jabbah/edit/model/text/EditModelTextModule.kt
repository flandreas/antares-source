package io.antarescircuit.jabbah.edit.model.text

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.Grid
import io.antarescircuit.jabbah.edit.GridPainterRegistry
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.model.rectangle.AbstractRectangularComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangularHandleSelectionModel
import io.antarescircuit.jabbah.edit.model.rectangle.RectangularReplaceSelectionModel
import io.antarescircuit.jabbah.edit.select.EditSelectModule
import io.antarescircuit.jabbah.edit.snap.DottedGridPainter
import io.antarescircuit.jabbah.edit.snap.LineGridPainter

/**
 * Module definitions for the [io.antarescircuit.jabbah.edit.model.text] package.
 */
object EditModelTextModule : AbstractModule() {

	var textComponentFactory: TextComponentFactory = UndefinedTextComponentFactory()

    override fun initialize() {
	    GridPainterRegistry.register(DottedGridPainter.NAME) { s -> DottedGridPainter(s) }
	    GridPainterRegistry.register(LineGridPainter.NAME) { s -> LineGridPainter(s) }

        EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.ABOVE,
            TextComponent::class)
        { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }

	    EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.ABOVE,
            SimpleTextComponent::class)
	    { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }

	    EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.REPLACE,
            SimpleTextComponent::class)
	    { RectangularReplaceSelectionModel(it as AbstractRectangularComponent) }

	    fillProperties(DrawModule.properties)
    }

	override fun resetDependencies() {}

	private fun fillProperties(properties: Properties) {
		properties.set(Grid.PROP_GRID_PAINTER, LineGridPainter.NAME)
	}
}