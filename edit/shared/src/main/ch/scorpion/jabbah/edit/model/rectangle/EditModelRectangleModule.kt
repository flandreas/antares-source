package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.model.rectangle] package.
 */
object EditModelRectangleModule : AbstractModule() {

    override fun initialize() {
        EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.BELOW,
            RectangularComponent::class.simpleName!!,
            {RectangularBelowSelectionModel(it as RectangularComponent)})

        EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.ABOVE,
            RectangularComponent::class.simpleName!!,
            { RectangularHandleSelectionModel(it as RectangularComponent) }
        )
    }
}