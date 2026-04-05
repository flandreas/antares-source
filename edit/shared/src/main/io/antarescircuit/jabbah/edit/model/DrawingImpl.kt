package io.antarescircuit.jabbah.edit.model

import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.model.text.description.Name

/**
 * Standard implementation of the [Drawing] interface.
 */
open class DrawingImpl<T : Component>(
    name: Name = Name("Drawing")
) : ComponentContainerImpl<T>(), Drawing<T> {

	override var name: Name = name

    override fun dispose() {
        // empty
    }
}