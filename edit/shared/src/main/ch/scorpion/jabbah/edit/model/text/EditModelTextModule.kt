package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangularHandleSelectionModel
import ch.scorpion.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.model.text] package.
 */
object EditModelTextModule : AbstractModule() {

    override fun initialize() {
        EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.ABOVE,
            TextComponent::class.simpleName!!,
            { RectangularHandleSelectionModel(it as RectangularComponent) })

        EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.ABOVE,
            SimpleTextComponent::class.simpleName!!,
            { RectangularHandleSelectionModel(it as RectangularComponent) })
    }
}