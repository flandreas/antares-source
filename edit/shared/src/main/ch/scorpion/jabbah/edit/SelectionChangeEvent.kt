package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.View
/**
 * Sent by a [SelectionManager] whenever the current selection in a [DrawingView] has changed.
 *
 * @param view the [View] in which the [Component]'s selection has changed
 * @param type the type of the selection
 * @param components the [Component]s that has been selected or deselected. Note that this collection
 *      doesn't contain all currently selected [Component]s, but only the delta.
 */
data class SelectionChangeEvent(
        val view: DrawingView<out Drawing<Component>>,
        val type: Type,
        val components: Collection<Component>
) {
    constructor(view: DrawingView<out Drawing<Component>>, components: Collection<Component>, selected: Boolean) :
        this(view, if (selected) Type.SELECTED else Type.DESELECTED, components)

    enum class Type {
        SELECTED,
        DESELECTED
    }

    val selected: Boolean = type == Type.SELECTED
}