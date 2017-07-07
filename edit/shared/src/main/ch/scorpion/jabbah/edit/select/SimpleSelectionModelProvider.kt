package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.SelectionModelProvider

/**
 * A [SelectionModelProvider] implementation that doesn't cache and reuse [SelectionModel]s, but simply
 * creates a new one each time one is needed.
 */
class SimpleSelectionModelProvider(val factory: SelectionModelFactory) : SelectionModelProvider {

    override fun provideFor(component: Component, strategy: SelectionDrawingStrategy): SelectionModel<Component>? {
        val selectionModel = factory.create(component, strategy)
        selectionModel?.setup()
        return selectionModel

    }

    override fun release(selectionModel: SelectionModel<Component>) {
        selectionModel.dispose()
    }
}