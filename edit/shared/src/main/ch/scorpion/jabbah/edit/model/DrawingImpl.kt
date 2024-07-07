package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.model.text.description.Name

/**
 * Standard implementation of the [Drawing] interface.
 */
open class DrawingImpl<T : Component> : ComponentContainerImpl<T>(), Drawing<T> {

	override var name: Name = Name("Drawing")

    override fun dispose() {
        // empty
    }
}