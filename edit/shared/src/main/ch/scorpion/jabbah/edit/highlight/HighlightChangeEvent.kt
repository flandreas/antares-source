package ch.scorpion.jabbah.edit.highlight

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Highlighter

/**
 * Posted by a [Highlighter] on its [EventBus] whenever the current highlighting in a [DrawingView] has changed.
 */
data class HighlightChangeEvent(
    val view: DrawingView<out Drawing<Component>>,
    val highlighter: Highlighter,
    val type: Type,
    val components: Collection<Component>
) {
    constructor(view: DrawingView<out Drawing<Component>>, highlighter: Highlighter, components: Collection<Component>, highighted: Boolean):
        this(view, highlighter, if(highighted) Type.HIGHLIGHTED else Type.UNHIGHLIGHED, components)

    enum class Type {
        HIGHLIGHTED,
        UNHIGHLIGHED
    }

    val highlighted: Boolean = type == Type.HIGHLIGHTED
    val unhighlighted: Boolean = type == Type.UNHIGHLIGHED
}