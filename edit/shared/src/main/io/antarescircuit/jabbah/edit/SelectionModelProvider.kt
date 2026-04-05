package io.antarescircuit.jabbah.edit

/**
 * Provides [SelectionModel]s for [Component]s.
 */
interface SelectionModelProvider {

    /** Provides a [SelectionModel] for the specified [Component] and [SelectionDrawingStrategy] */
    fun provideFor(component: Component, strategy: SelectionDrawingStrategy): SelectionModel<Component>?

    /**
     * Releases a [SelectionModel] that was previously requested using [provideFor] and that is not used any more
     * to select its [Component].
     */
    fun release(selectionModel: SelectionModel<Component>)
}