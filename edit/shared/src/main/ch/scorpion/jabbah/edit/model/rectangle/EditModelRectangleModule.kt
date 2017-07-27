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
            RectangleComponent::class.simpleName!!,
            { RectangularBelowSelectionModel(it as RectangularComponent)} )

        EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.ABOVE,
            RectangleComponent::class.simpleName!!,
            { RectangularHandleSelectionModel(it as RectangularComponent) }
        )

        EditSelectModule.selectionModelFactory.register(
                SelectionDrawingStrategy.BELOW,
                EllipseComponent::class.simpleName!!,
                { RectangularBelowSelectionModel(it as RectangularComponent)} )

        EditSelectModule.selectionModelFactory.register(
                SelectionDrawingStrategy.ABOVE,
                EllipseComponent::class.simpleName!!,
                { RectangularHandleSelectionModel(it as RectangularComponent) }
        )

        EditSelectModule.selectionModelFactory.register(
                SelectionDrawingStrategy.BELOW,
                RoundRectangleComponent::class.simpleName!!,
                { RectangularBelowSelectionModel(it as RectangularComponent)} )

        EditSelectModule.selectionModelFactory.register(
                SelectionDrawingStrategy.ABOVE,
                RoundRectangleComponent::class.simpleName!!,
                { RectangularHandleSelectionModel(it as RectangularComponent) }
        )
    }
}