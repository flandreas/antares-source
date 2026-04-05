package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing

/**
 * Customizes a [Component] while/after being added to a [Drawing].
 */
interface ComponentCustomizer {

    /**
     * Used by method that adds [Component]s to a [Drawing] (and the corresponding [Command]s)
     * to customize the properties of the [Component] after it has been added to the [Drawing].
     * Can for example be used to apply default from the [Drawing] (such as default colors)
     * to added [Component]s.
     */
    fun customizeAddedComponent(component: Component, drawing: Drawing<*>)
}

class ComponentCustomizerPair(
    private val c1: ComponentCustomizer,
    private val c2: ComponentCustomizer
) : ComponentCustomizer {

    override fun customizeAddedComponent(component: Component, drawing: Drawing<*>) {
        c1.customizeAddedComponent(component, drawing)
        c2.customizeAddedComponent(component, drawing)
    }
}