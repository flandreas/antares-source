package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.SelectionModel
import kotlin.reflect.KClass

/**
 * A factory for creating [SelectionModel]s.
 */
interface SelectionModelFactory {

    /**
     * Creates a [SelectionModel] for the specified [Component] that is to be used for a particular [SelectionDrawingStrategy].
     * @return ´null´ if no suitable [SelectionModel] could be created.
     */
    fun create(component: Component, strategy: SelectionDrawingStrategy): SelectionModel<Component>?

    /**
     * Registers a factory that creates a [SelectionModel] for a particular [Component]'s class name when
     * using a particular [SelectionDrawingStrategy].
     *
     * It would have been more elegant to use classes for both the [Component] and the [SelectionModel] to
     * be created. Unfortunately, the JavaScript compiler of Kotlin currently doesn't support more
     * reflection functionality that retrieving the simple name of a class.
     */
    fun register(strategy: SelectionDrawingStrategy, componentClass: KClass<*>, factory: (Component) -> SelectionModel<Component>)
}