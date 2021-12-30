package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing

/**
 * Standard implementation of the [Drawing] interface.
 */
open class DrawingImpl<T : Component> : ComponentContainerImpl<T>(), Drawing<T> {

	override val name: String get() = "Drawing"

    override fun dispose() {
        // empty
    }
}