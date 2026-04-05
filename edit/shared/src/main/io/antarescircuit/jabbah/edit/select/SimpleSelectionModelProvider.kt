package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.SelectionModelProvider

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